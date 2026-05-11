"""
Generate SmartPantry sensor CSV files from real Kaggle environmental datasets.

Sources:
  - Stafford 2020 Environmental Sensor Telemetry Data
    https://www.kaggle.com/datasets/garystafford/environmental-sensor-data-132k
  - Ziya 2025 IoT-Based Environmental Dataset
    https://www.kaggle.com/datasets/ziya07/iot-based-environmental-dataset

Mapping rationale (cite this in the A4 report under "Data sources" / §9 context-aware computing):

  - fridge.csv   <- Stafford device b8:27:eb:bf:9d:51 (stable indoor, base ~22.6 C)
                    Transform: tempC = stafford_temp - 18  (shifts to typical fridge 1-7 C range)
                    Breach injection: ~6% of rows sourced from device 1c:bf:ce:15:ec:4d
                    (warmer indoor, base ~27 C); same -18 offset puts them at ~9 C,
                    above the fridge threshold of 7 C => natural alert triggers.
                    Humidity: Stafford device 1 averages ~50%, already realistic for fridge.
                    Lux: Stafford has boolean `light`; we map false->0, true->50-200 (door open).

  - freezer.csv  <- Stafford device 00:0f:00:70:91:0a (cool indoor, base ~19.7 C)
                    Transform: tempC = stafford_temp - 37  (shifts to -22 to -16 C freezer range)
                    Breach injection: ~5% from device 3, same -37 offset puts at ~-10 C,
                    above freezer threshold of -15 C => alert.
                    Humidity: Stafford device 2 averages ~76% which is unrealistic for a freezer;
                    we scale humidity * 0.45 to land in the 30-40% range typical of frost-free units.
                    Lux: same boolean-to-lux mapping as fridge.

  - pantry.csv   <- Ziya dataset (university room measurements, base 18-30 C)
                    Temperature and humidity used DIRECTLY: temperature_celsius -> tempC,
                    humidity_percent -> humidity. Lux is scaled by 0.5 to approximate the
                    dimmer ambient light of a closed kitchen pantry (Ziya was measured in
                    open university spaces under continuous overhead lighting; a closed
                    pantry has much lower baseline ambient light). ~8% of rows have an
                    injected "door-open" lux spike (sampled from Ziya's top-quartile lux
                    rows then amplified) to simulate sunlight or kitchen lights breaching
                    the 300 lux threshold for short bursts. Temperature breaches (>28 C)
                    occur naturally in ~5-8% of rows from the Ziya distribution.

The transformations preserve the noise patterns, sample-to-sample variance, and timing
characteristics of the source datasets - only the absolute temperature scale is offset to
match refrigeration physics. Humidity and light behaviour reflect the real sensor data.

Output: app/src/main/assets/sensors/{fridge,freezer,pantry}.csv
        Header: epochSec,tempC,humidity,lux
        ~200 rows per file, 15-min cadence, ~6-8% rows in breach range.
"""

from __future__ import annotations

import csv
import random
import sys
from pathlib import Path

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

random.seed(20260511)  # reproducible: same seed -> same output

# Adjust these paths if you've moved the Kaggle CSV files.
KAGGLE_DIR = Path(r"D:\Docs\course materials\FIT5046\assignment 4\kaggle data")
STAFFORD_CSV = KAGGLE_DIR / "stafford_iot_telemetry_data.csv"
ZIYA_CSV = KAGGLE_DIR / "ziya_university_mental_health_iot_dataset.csv"

# Output directory: app/src/main/assets/sensors/ relative to this script's grandparent.
SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parent
OUT_DIR = REPO_ROOT / "app" / "src" / "main" / "assets" / "sensors"

# CSV generation parameters
START_EPOCH = 1767139200  # 2026-01-01 00:00:00 UTC
INTERVAL_SEC = 900        # 15 minutes between rows
N_ROWS = 200              # rows per output file

# Stafford device IDs (3 distinct physical sensors)
DEVICE_STABLE_INDOOR = "b8:27:eb:bf:9d:51"   # ~22.6 C, fridge base
DEVICE_COOL_INDOOR   = "00:0f:00:70:91:0a"   # ~19.7 C, freezer base
DEVICE_WARM_INDOOR   = "1c:bf:ce:15:ec:4d"   # ~27 C,   breach injection source

# Storage thresholds (used only for breach-rate reporting)
THRESHOLDS = {
    "fridge":  {"tempC": 7.0,   "lux": None},
    "freezer": {"tempC": -15.0, "lux": None},
    "pantry":  {"tempC": 28.0,  "lux": 300.0},
}


# ---------------------------------------------------------------------------
# Data loaders
# ---------------------------------------------------------------------------

def load_stafford_by_device() -> dict[str, list[dict]]:
    """Stream Stafford CSV (60 MB) once; group rows by device MAC."""
    by_device: dict[str, list[dict]] = {
        DEVICE_STABLE_INDOOR: [],
        DEVICE_COOL_INDOOR: [],
        DEVICE_WARM_INDOOR: [],
    }
    if not STAFFORD_CSV.exists():
        sys.exit(f"ERROR: Stafford CSV not found at {STAFFORD_CSV}")
    with STAFFORD_CSV.open(newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            dev = row.get("device", "")
            if dev not in by_device:
                continue
            try:
                by_device[dev].append({
                    "temp": float(row["temp"]),
                    "humidity": float(row["humidity"]),
                    "light": row["light"].strip().lower() == "true",
                })
            except (KeyError, ValueError):
                continue
    return by_device


def load_ziya() -> list[dict]:
    """Load Ziya dataset fully into memory (it's small)."""
    rows: list[dict] = []
    if not ZIYA_CSV.exists():
        sys.exit(f"ERROR: Ziya CSV not found at {ZIYA_CSV}")
    with ZIYA_CSV.open(newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            try:
                rows.append({
                    "temp": float(row["temperature_celsius"]),
                    "humidity": float(row["humidity_percent"]),
                    "lux": float(row["lighting_lux"]),
                })
            except (KeyError, ValueError):
                continue
    return rows


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def evenly_sample(seq: list, n: int) -> list:
    """Take n items evenly spaced across seq; preserves source order."""
    if len(seq) <= n:
        return list(seq)
    step = len(seq) / n
    return [seq[int(i * step)] for i in range(n)]


def light_bool_to_lux(
    light_on: bool,
    off_range: tuple[float, float] = (0.0, 0.0),
    on_range: tuple[float, float] = (50.0, 200.0),
) -> float:
    """Convert Stafford's boolean `light` to a plausible lux float."""
    lo, hi = on_range if light_on else off_range
    return round(random.uniform(lo, hi), 1)


# ---------------------------------------------------------------------------
# Per-sensor generators
# ---------------------------------------------------------------------------

def gen_fridge(stafford: dict[str, list[dict]]) -> list[dict]:
    """Fridge: stable indoor base, -18 C offset, ~6% breach injection."""
    normal_pool = stafford[DEVICE_STABLE_INDOOR]
    breach_pool = stafford[DEVICE_WARM_INDOOR]

    n_breach = max(int(N_ROWS * 0.06), 8)        # ~12 rows
    n_normal = N_ROWS + 5 - n_breach              # oversample slightly for safety

    normals = evenly_sample(normal_pool, n_normal)
    breaches = evenly_sample(breach_pool, n_breach)

    rows: list[dict] = []
    for src in normals:
        rows.append({
            "tempC": round(src["temp"] - 18.0, 2),
            "humidity": round(src["humidity"], 1),
            "lux": light_bool_to_lux(src["light"], (0.0, 0.0), (50.0, 200.0)),
        })
    for src in breaches:
        rows.append({
            "tempC": round(src["temp"] - 18.0, 2),
            "humidity": round(src["humidity"], 1),
            # Breach rows have higher chance of "door open" lux spike
            "lux": light_bool_to_lux(src["light"], (0.0, 5.0), (100.0, 300.0)),
        })

    random.shuffle(rows)
    return rows[:N_ROWS]


def gen_freezer(stafford: dict[str, list[dict]]) -> list[dict]:
    """Freezer: cool indoor base, -37 C offset, humidity scaled, ~5% breach."""
    normal_pool = stafford[DEVICE_COOL_INDOOR]
    breach_pool = stafford[DEVICE_WARM_INDOOR]

    n_breach = max(int(N_ROWS * 0.05), 8)        # ~10 rows
    n_normal = N_ROWS + 5 - n_breach

    normals = evenly_sample(normal_pool, n_normal)
    breaches = evenly_sample(breach_pool, n_breach)

    rows: list[dict] = []
    for src in normals:
        rows.append({
            "tempC": round(src["temp"] - 37.0, 2),
            "humidity": round(src["humidity"] * 0.45, 1),  # scale 76% -> ~34%
            "lux": light_bool_to_lux(src["light"], (0.0, 0.0), (50.0, 150.0)),
        })
    for src in breaches:
        rows.append({
            "tempC": round(src["temp"] - 37.0, 2),
            "humidity": round(src["humidity"] * 0.45, 1),
            "lux": light_bool_to_lux(src["light"], (0.0, 5.0), (100.0, 200.0)),
        })

    random.shuffle(rows)
    return rows[:N_ROWS]


def gen_pantry(ziya: list[dict]) -> list[dict]:
    """Pantry: Ziya temp/humidity as-is; lux scaled to closed-pantry baseline (* 0.5),
    with ~8% rows getting an injected high-lux burst (door open / sunlight)."""
    LUX_SCALE = 0.5         # ambient daylight in closed pantry << open uni building
    BREACH_LUX_RATE = 0.08  # ~16 of 200 rows simulate brief lighting events

    # Sort Ziya by lux to identify the top-quartile rows (used as breach-injection source).
    ziya_sorted = sorted(ziya, key=lambda r: r["lux"], reverse=True)
    top_quartile = ziya_sorted[: len(ziya) // 4]

    n_breach = max(int(N_ROWS * BREACH_LUX_RATE), 12)
    n_normal = N_ROWS + 5 - n_breach

    normals = evenly_sample(ziya, n_normal)
    breach_sources = evenly_sample(top_quartile, n_breach)

    rows: list[dict] = []
    for src in normals:
        rows.append({
            "tempC": round(src["temp"], 2),
            "humidity": round(src["humidity"], 1),
            # Scale lux to typical closed-pantry baseline; preserves Ziya's variance shape.
            "lux": round(src["lux"] * LUX_SCALE, 1),
        })
    for src in breach_sources:
        # Amplify top-quartile lux 2.0x to push it confidently above the 300 lx breach threshold.
        rows.append({
            "tempC": round(src["temp"], 2),
            "humidity": round(src["humidity"], 1),
            "lux": round(src["lux"] * 2.0, 1),
        })

    random.shuffle(rows)
    return rows[:N_ROWS]


# ---------------------------------------------------------------------------
# Writer
# ---------------------------------------------------------------------------

def write_csv(name: str, rows: list[dict]) -> None:
    path = OUT_DIR / f"{name}.csv"
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="\n", encoding="utf-8") as f:
        f.write("epochSec,tempC,humidity,lux\n")
        for i, r in enumerate(rows):
            t = START_EPOCH + i * INTERVAL_SEC
            f.write(f"{t},{r['tempC']},{r['humidity']},{r['lux']}\n")

    thresholds = THRESHOLDS[name]
    breach_count = 0
    for r in rows:
        temp_breach = thresholds["tempC"] is not None and r["tempC"] > thresholds["tempC"]
        lux_breach = thresholds["lux"] is not None and r["lux"] > thresholds["lux"]
        if temp_breach or lux_breach:
            breach_count += 1
    pct = breach_count / len(rows) * 100 if rows else 0.0
    print(f"  {path.name:12s} {len(rows):>4d} rows, {breach_count:>3d} breach ({pct:.1f}%)")


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    print(f"Loading Stafford dataset from {STAFFORD_CSV.name} (60 MB, this may take ~30 s)...")
    stafford = load_stafford_by_device()
    for dev, rows in stafford.items():
        print(f"  device {dev}: {len(rows):>6d} rows")

    print(f"Loading Ziya dataset from {ZIYA_CSV.name}...")
    ziya = load_ziya()
    print(f"  {len(ziya):>6d} rows")

    print(f"\nWriting sensor CSVs to {OUT_DIR}...")
    write_csv("fridge", gen_fridge(stafford))
    write_csv("freezer", gen_freezer(stafford))
    write_csv("pantry", gen_pantry(ziya))

    print("\nDone. Inspect with:")
    print(f"  Get-Content {OUT_DIR / 'fridge.csv'} -Head 10")


if __name__ == "__main__":
    main()
