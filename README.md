# smart-pantry

## 🚀 Project Branching Protocol

### 1. Branch Structure
    *   **`master`**: Production branch. Contains the final, stable code ready for submission.
    *   **`dev`**: Integration branch. The primary branch for daily development. All completed features are merged here.
    *   **`test`**: Testing branch. Used for final integration testing, bug fixing, and pre-demo preparation.

### 2. Feature Workflow
**Core Principle: One feature, one branch.**

1. **Sync with `dev`**: Always start with the most up-to-date code.
    ```bash
    git checkout dev
    git pull
    ```

2.  **Create a Feature Branch**: Branch off from dev. Use the naming convention feature/<your-name>-<feature-name>. For example, feature/jim-login.
    ```bash
    git checkout -b feature/jim-login
    ```

3.  **Develop Locally**: Make small, frequent commits with descriptive messages.
4.  **Push and Merge**:
    *   Push your branch to the remote repository: 
        ```bash
        git push origin feature/jim-login
        ```
    *   Open a **Pull Request (PR)** on GitHub/GitLab targeting the `dev` branch.
    *   Assign at least one teammate for **Code Review**. Merge only after their approval.
    *   Clean Up: Once merged, delete your feature branch (both locally and remotely) and return to dev for your next task.

### 3. The 3 "Don'ts" of Collaboration
    *   No Direct Pushes: Never commit directly to dev or main. All changes must go through a PR.
    *   No Broken Code: Never merge code that fails to compile or contains obvious errors.
    *   No Long-Lived Branches: Keep feature branches short-lived (ideally under 3 days). If a feature is too large, break it down into smaller sub-tasks.

### 4. Conflict Resolution
    If you encounter a merge conflict during a PR:
    
    *   Switch to your local feature branch.
    *   Run git merge dev (or git pull origin dev).
    *   Resolve the conflicts locally, test the code to ensure it works, commit the changes, and push again.

## Getting started

To make it easy for you to get started with GitLab, here's a list of recommended next steps.

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!

## Add your files

* [Create](https://docs.gitlab.com/user/project/repository/web_editor/#create-a-file) or [upload](https://docs.gitlab.com/user/project/repository/web_editor/#upload-a-file) files
* [Add files using the command line](https://docs.gitlab.com/topics/git/add_files/#add-files-to-a-git-repository) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin https://gitlab.com/szhe0057/smart-pantry.git
git branch -M main
git push -uf origin main
```

## Integrate with your tools

* [Set up project integrations](https://gitlab.com/szhe0057/smart-pantry/-/settings/integrations)

## Collaborate with your team

* [Invite team members and collaborators](https://docs.gitlab.com/user/project/members/)
* [Create a new merge request](https://docs.gitlab.com/user/project/merge_requests/creating_merge_requests/)
* [Automatically close issues from merge requests](https://docs.gitlab.com/user/project/issues/managing_issues/#closing-issues-automatically)
* [Enable merge request approvals](https://docs.gitlab.com/user/project/merge_requests/approvals/)
* [Set auto-merge](https://docs.gitlab.com/user/project/merge_requests/auto_merge/)

## Test and Deploy

Use the built-in continuous integration in GitLab.

* [Get started with GitLab CI/CD](https://docs.gitlab.com/ci/quick_start/)
* [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/user/application_security/sast/)
* [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/topics/autodevops/requirements/)
* [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/user/clusters/agent/)
* [Set up protected environments](https://docs.gitlab.com/ci/environments/protected_environments/)

***

# Editing this README

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thanks to [makeareadme.com](https://www.makeareadme.com/) for this template.

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.

## SQL for initialising data

-- ============= CLEANUP =============
DELETE FROM food_items;
DELETE FROM sensor_readings;
DELETE FROM waste_log;
DELETE FROM notifications;
DELETE FROM shopping;

-- ============= FOOD ITEMS (16 active + 6 consumed = 22 rows; 17 columns post-v6) =============
INSERT INTO food_items (id, name, category, quantity, unit, barcode, imageUrl, purchaseDate, nominalExpiryDate, effectiveExpiryDate, storageLocationId, storageChangedAt, notes, createdAt, updatedAt, consumed, wasted) VALUES
('food-milk',            'Whole Milk',                    'Dairy',     '1',   'L',  NULL,            NULL, 1778544000000, 1779840000000, 1779667200000, 'sensor-fridge',  1778544000000, 'Recalc shifted forward by 2 days due to fridge breaches on 5/22 and 5/24 — great Q10-lite demo', 1778544000000, 1779631200000, 0, 0),
('food-spinach',         'Baby Spinach',                  'Produce',   '200', 'g',  NULL,            NULL, 1778544000000, 1779753600000, 1779753600000, 'sensor-fridge',  1778544000000, NULL, 1778544000000, 1778544000000, 0, 0),
('food-yogurt',          'Greek Yogurt',                  'Dairy',     '500', 'g',  '9300675013642', NULL, 1778544000000, 1779840000000, 1779840000000, 'sensor-fridge',  1778544000000, 'Coyo brand — single-serve cups', 1778544000000, 1778544000000, 0, 0),
('food-chicken',         'Chicken Breast',                'Meat',      '500', 'g',  NULL,            NULL, 1779062400000, 1779926400000, 1779926400000, 'sensor-fridge',  1779062400000, 'Free-range', 1779062400000, 1779062400000, 0, 0),
('food-tomatoes',        'Cherry Tomatoes',               'Produce',   '300', 'g',  NULL,            NULL, 1779062400000, 1780012800000, 1780012800000, 'sensor-fridge',  1779062400000, NULL, 1779062400000, 1779062400000, 0, 0),
('food-salmon',          'Salmon Fillet',                 'Meat',      '300', 'g',  NULL,            NULL, 1779062400000, 1780099200000, 1780099200000, 'sensor-freezer', 1779062400000, 'Tasmanian Atlantic — to be cooked, not long-frozen', 1779062400000, 1779062400000, 0, 0),
('food-strawberries',    'Strawberries',                  'Produce',   '250', 'g',  NULL,            NULL, 1779062400000, 1780099200000, 1780099200000, 'sensor-fridge',  1779062400000, NULL, 1779062400000, 1779062400000, 0, 0),
('food-cucumber',        'Cucumber',                      'Produce',   '2',   'pcs','9300675000123', NULL, 1779062400000, 1780185600000, 1780185600000, 'sensor-fridge',  1779062400000, NULL, 1779062400000, 1779062400000, 0, 0),
('food-hummus',          'Hummus',                        'Dairy',     '250', 'g',  NULL,            NULL, 1779062400000, 1780272000000, 1780272000000, 'sensor-fridge',  1779062400000, NULL, 1779062400000, 1779062400000, 0, 0),
('food-carrots',         'Carrots',                       'Produce',   '500', 'g',  NULL,            NULL, 1779062400000, 1780358400000, 1780358400000, 'sensor-fridge',  1779062400000, NULL, 1779062400000, 1779062400000, 0, 0),
('food-bread',           'Bread Loaf',                    'Dry Goods', '700', 'g',  '9300601003228', NULL, 1779062400000, 1780444800000, 1780444800000, 'sensor-pantry',  1779062400000, NULL, 1779062400000, 1779062400000, 0, 0),
('food-pasta',           'Spaghetti Pasta',               'Dry Goods', '500', 'g',  NULL,            NULL, 1777939200000, 1795651200000, 1795651200000, 'sensor-pantry',  1777939200000, 'San Remo #5', 1777939200000, 1777939200000, 0, 0),
('food-rice',            'Jasmine Rice',                  'Dry Goods', '2',   'kg', NULL,            NULL, 1777939200000, 1795651200000, 1795651200000, 'sensor-pantry',  1777939200000, 'Sunwhite', 1777939200000, 1777939200000, 0, 0),
('food-peas',            'Frozen Peas',                   'Frozen',    '500', 'g',  NULL,            NULL, 1777939200000, 1795651200000, 1795651200000, 'sensor-freezer', 1777939200000, NULL, 1777939200000, 1777939200000, 0, 0),
('food-tuna',            'Canned Tuna',                   'Other',     '185', 'g',  NULL,            NULL, 1777334400000, 1811289600000, 1811289600000, 'sensor-pantry',  1777334400000, 'In springwater', 1777334400000, 1777334400000, 0, 0),
('food-olive-oil',       'Olive Oil',                     'Other',     '500', 'ml', NULL,            NULL, 1776556800000, 1811289600000, 1811289600000, NULL,             1776556800000, 'On the kitchen shelf — no sensor', 1776556800000, 1776556800000, 0, 0),
('food-consumed-apples', 'Royal Gala Apples',             'Produce',   '6',   'pcs',NULL,            NULL, 1779062400000, 1780099200000, 1780099200000, 'sensor-fridge',  1779062400000, NULL, 1779062400000, 1779631200000, 1, 0),
('food-consumed-eggs',   'Eggs (12 pack)',                'Dairy',     '12',  'pcs',NULL,            NULL, 1778544000000, 1795651200000, 1795651200000, 'sensor-fridge',  1778544000000, 'Free-range', 1778544000000, 1779372000000, 1, 0),
('food-hist-yogurt',     'Greek Yogurt (older batch)',    'Dairy',     '250', 'g',  NULL,            NULL, 1778544000000, 1779148800000, 1779148800000, 'sensor-fridge',  1778544000000, NULL, 1778544000000, 1778803200000, 1, 0),
('food-hist-bananas',    'Bananas',                       'Produce',   '6',   'pcs',NULL,            NULL, 1777939200000, 1778544000000, 1778544000000, 'sensor-fridge',  1777939200000, NULL, 1777939200000, 1778371200000, 1, 0),
('food-hist-pasta',      'Spaghetti Pasta (older batch)', 'Dry Goods', '500', 'g',  NULL,            NULL, 1776211200000, 1792022400000, 1792022400000, 'sensor-pantry',  1776211200000, 'Bought before this batch', 1776211200000, 1777334400000, 1, 0),
('food-hist-rice',       'Jasmine Rice (older batch)',    'Dry Goods', '1',   'kg', NULL,            NULL, 1775347200000, 1806883200000, 1806883200000, 'sensor-pantry',  1775347200000, NULL, 1775347200000, 1776643200000, 1, 0);

-- ============= SENSOR READINGS (27 rows = 9 per sensor × 3) =============
INSERT INTO sensor_readings (id, sensorId, timestamp, temperatureC, humidity, lux, breachFlag) VALUES
(NULL, 'sensor-fridge',  1779199200000,   4.2, 55.0,  15.0, 0),
(NULL, 'sensor-fridge',  1779285600000,   4.5, 58.0,  18.0, 0),
(NULL, 'sensor-fridge',  1779372000000,   4.0, 56.0,  12.0, 0),
(NULL, 'sensor-fridge',  1779458400000,   8.5, 70.0,  22.0, 1),
(NULL, 'sensor-fridge',  1779544800000,   4.8, 60.0,  20.0, 0),
(NULL, 'sensor-fridge',  1779631200000,   9.2, 75.0,  25.0, 1),
(NULL, 'sensor-fridge',  1779717600000,   5.1, 58.0,  18.0, 0),
(NULL, 'sensor-fridge',  1779782400000,   4.5, 57.0,  14.0, 0),
(NULL, 'sensor-fridge',  1779804000000,   4.3, 56.0,  15.0, 0),
(NULL, 'sensor-freezer', 1779199200000, -17.2, 30.0,   5.0, 0),
(NULL, 'sensor-freezer', 1779285600000, -16.8, 32.0,   8.0, 0),
(NULL, 'sensor-freezer', 1779372000000, -12.5, 35.0,  10.0, 1),
(NULL, 'sensor-freezer', 1779458400000, -17.0, 30.0,   5.0, 0),
(NULL, 'sensor-freezer', 1779544800000, -16.5, 31.0,   6.0, 0),
(NULL, 'sensor-freezer', 1779631200000, -13.1, 38.0,  12.0, 1),
(NULL, 'sensor-freezer', 1779717600000, -17.5, 30.0,   5.0, 0),
(NULL, 'sensor-freezer', 1779782400000, -17.0, 30.0,   6.0, 0),
(NULL, 'sensor-freezer', 1779804000000, -16.8, 30.0,   7.0, 0),
(NULL, 'sensor-pantry',  1779199200000,  24.5, 50.0, 180.0, 0),
(NULL, 'sensor-pantry',  1779285600000,  25.1, 48.0, 200.0, 0),
(NULL, 'sensor-pantry',  1779372000000,  30.2, 55.0, 220.0, 1),
(NULL, 'sensor-pantry',  1779458400000,  24.8, 52.0, 190.0, 0),
(NULL, 'sensor-pantry',  1779544800000,  27.5, 50.0, 450.0, 4),
(NULL, 'sensor-pantry',  1779631200000,  31.5, 58.0, 480.0, 5),
(NULL, 'sensor-pantry',  1779717600000,  25.3, 51.0, 210.0, 0),
(NULL, 'sensor-pantry',  1779782400000,  25.8, 50.0, 215.0, 0),
(NULL, 'sensor-pantry',  1779804000000,  26.0, 50.0, 220.0, 0);

-- ============= WASTE LOG (14 entries — 6 Whole Milk drives Smart Suggestions; ★ = preserved from original test-data-seed.md) =============
INSERT INTO waste_log (id, foodItemId, name, category, quantity, wastedAt, reason, co2eGrams) VALUES
('waste-01', NULL, 'Whole Milk',    'Dairy',     '0.5 kg',  1779408000000, 'EXPIRED',  9000.0),  -- May 22
('waste-02', NULL, 'Lettuce',       'Produce',   '0.2 kg',  1779062400000, 'OTHER',     400.0),  -- May 18
('waste-03', NULL, 'Whole Milk',    'Dairy',     '0.4 kg',  1778889600000, 'EXPIRED',  7200.0),  -- May 16 ★
('waste-04', NULL, 'Whole Milk',    'Dairy',     '0.3 kg',  1778716800000, 'SPOILED',  5400.0),  -- May 14
('waste-05', NULL, 'Cheese Block',  'Dairy',     '0.2 kg',  1778284800000, 'EXPIRED',  3600.0),  -- May  9
('waste-06', NULL, 'Whole Milk',    'Dairy',     '0.3 kg',  1778112000000, 'SPOILED',  5400.0),  -- May  7 ★
('waste-07', NULL, 'Whole Milk',    'Dairy',     '0.4 kg',  1777766400000, 'EXPIRED',  7200.0),  -- May  3
('waste-08', NULL, 'Salmon Slice',  'Meat',      '0.3 kg',  1777420800000, 'EXPIRED',  8100.0),  -- Apr 29 ★
('waste-09', NULL, 'Whole Milk',    'Dairy',     '0.4 kg',  1776988800000, 'EXPIRED',  7200.0),  -- Apr 24 ★
('waste-10', NULL, 'Cucumber',      'Produce',   '0.15 kg', 1776729600000, 'OTHER',     300.0),  -- Apr 21 ★
('waste-11', NULL, 'Ground Beef',   'Meat',      '0.4 kg',  1776470400000, 'SPOILED', 10800.0),  -- Apr 18
('waste-12', NULL, 'Bread Loaf',    'Dry Goods', '0.7 kg',  1774828800000, 'OTHER',    1050.0),  -- Mar 30
('waste-13', NULL, 'Chicken Thigh', 'Meat',      '0.5 kg',  1774483200000, 'SPOILED', 13500.0),  -- Mar 26
('waste-14', NULL, 'Ground Beef',   'Meat',      '0.4 kg',  1773964800000, 'SPOILED', 10800.0); -- Mar 20 ★

-- ============= NOTIFICATIONS (8 entries — 4 unread, 4 read) =============
INSERT INTO notifications (id, type, title, body, relatedFoodId, relatedSensorId, createdAt, read) VALUES
('notif-1', 'SENSOR_BREACH', 'Kitchen Fridge alert', 'Temperature 9.2°C exceeds threshold 7.0°C',                                                            NULL,           'sensor-fridge',  1779631200000, 0),
('notif-2', 'SENSOR_BREACH', 'Pantry Shelf alert',   'Light 480 lux exceeds maximum 300 lux; Temperature 31.5°C exceeds threshold 28.0°C',                   NULL,           'sensor-pantry',  1779631200000, 0),
('notif-3', 'EXPIRY_SOON',   'Baby Spinach expires today', 'Use it before it goes off — 0 days left.',                                                       'food-spinach', NULL,             1779782400000, 0),
('notif-4', 'EXPIRY_SOON',   'Whole Milk: shelf life shortened', 'Storage breaches reduced shelf life by 2 days — use today.',                              'food-milk',    NULL,             1779782400000, 0),
('notif-5', 'SENSOR_BREACH', 'Kitchen Fridge alert', 'Temperature 8.5°C exceeds threshold 7.0°C',                                                            NULL,           'sensor-fridge',  1779458400000, 1),
('notif-6', 'EXPIRY_SOON',   'Greek Yogurt expires tomorrow', 'Only 1 day left — Yogurt is in Kitchen Fridge.',                                              'food-yogurt',  NULL,             1779782400000, 1),
('notif-7', 'SHOPPING',      'Restock suggestion',   'You have wasted Whole Milk 6 times recently — see Smart Suggestions on the Shopping list.',           NULL,           NULL,             1779364800000, 1),
('notif-8', 'SENSOR_BREACH', 'Freezer alert',        'Temperature -12.5°C exceeds threshold -15.0°C',                                                        NULL,           'sensor-freezer', 1779372000000, 1);

-- ============= SHOPPING (6 items — 5 unchecked, 1 checked) =============
INSERT INTO shopping (id, name, quantity, unit, category, checked, addedAt, nominalExpiryDate, storageLocationId, autoSuggested) VALUES
('shop-1', 'Almond Milk',  '1',   'L',   'Dairy',     0, 1779796800000, 1780617600000, 'sensor-fridge', 0),
('shop-2', 'Brown Bread',  '700', 'g',   'Dry Goods', 0, 1779796800000, 1780617600000, 'sensor-pantry', 0),
('shop-3', 'Bananas',      '6',   'pcs', 'Produce',   0, 1779796800000, 1780358400000, 'sensor-fridge', 0),
('shop-4', 'Greek Yogurt', '500', 'g',   'Dairy',     0, 1779796800000, 1780963200000, 'sensor-fridge', 0),
('shop-5', 'Eggs',         '12',  'pcs', 'Dairy',     0, 1779717600000, 1795651200000, 'sensor-fridge', 0),
('shop-6', 'Coffee Beans', '500', 'g',   'Other',     1, 1779631200000, 1811289600000, 'sensor-pantry', 0);
