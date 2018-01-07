## Team 6458's FIRST POWER UP 2018

Welcome to Team 6458's code repository for the 2018 FIRST POWER UP
robotics competition.

## Resources
* [2018 FRC Control System](https://wpilib.screenstepslive.com/s/4485)

## "This looks really scary"
Yes. It is certainly overwhelming, and total beginners are probably NOT going to
have the smoothest transition over.
You don't ever have to write code that gets accepted if it's beyond your
skill level.
However, you can always contribute in other
ways, such as reviewing code in pull requests. The more eyes, the better!

## Pull Request Reviewing
Reviewing others' pull requests is an **essential** part of the workflow
we have. Without reviews, bad code tends to get put in and malfunctions
and bugs occur. Last year, only **one** person wrote the entire robot's
systems, and the number of **human injuries** that nearly occurred were far
too frequent. To avoid damage to the robot and human injuries
(humans heal--robots don't), we need to make sure all possible sources of
error are filtered out first and foremost--especially before testing on the real thing.

## Installation
1. Ensure you have [Git](https://git-scm.com/) and [JDK **8**](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) installed.
2. Make a new directory somewhere, enter it, and start Git Bash in that directory. You can do this on Windows by right clicking in the File Explorer and clicking Git Bash Here.
3. Type `git clone https://github.com/chrislo27/Team6458-2018` and press Enter.
4. Using IntelliJ IDEA or Eclipse, open this as a Gradle Project with the build.gradle file inside.
5. If you are using IntelliJ, make sure that your project settings use the
code style outlined at [.idea/codeStyles/Project.xml](.idea/codeStyles/Project.xml).

### Keeping the `master` branch up to date
0. Have no unstaged changes.
1. `git checkout master && git pull --rebase origin master`
2. `git checkout <your old branch here>`

### To update your own branch
0. Have no unstaged changes.
1. Ensure the `master` branch is up to date (see above).
2. While checked out in your branch: `git rebase master`

### Submitting Changes
0. Do all your work on a new branch first, not master.
Do this by running this command with Git Bash INSIDE of the folder where
the `.git/` folder is: `git branch <branch name>`. The branch name should
clearly indicate it belongs to you.
1. After committing and pushing your work to `origin`, open a Pull Request. It is one of the tabs on the page.
2. Have a clear title and description.
3. Everyone will begin to review your work to see if it is fit for merging into the master branch. At least one of the [code owners](.github/CODEOWNERS) must approve it.
4. If everything looks good, the owner of this repository will merge it.

## Code Specifications
* Java **8** only. No other JVM languages please. (The `src/unused/` folder contains
unused code only for reference, and it should not be tampered with.)
  * Java 9 is not supported, unfortunately.
* Use `private static final Logger LOGGER` instances where logging is necessary. See
[SemiRobot.java](src/main/java/team6458/SemiRobot.java) for an example.

### Formatting
* Four spaces or tabs set to four space widths
* Correct indentations for all code
* Keep it neat, please. This won't be nitpicked to a T, but it shouldn't
look like a bomb went off.

### Javadocs
* **EVERYTHING** needs Javadoc documentation, unless it is:
  * A `private static final Logger LOGGER` instance in a class
  * An obvious getter/setter
* All documentation must be grammatically correct English with no spelling errors
  * Note: text for `@tags` like `@param` or `@returns` do not need a full stop
* See [PlateAssignment.java](src/main/java/team6458/util/PlateAssignment.java) for a thorough example.
* Tip: write your documentation like a serial killer is out to get you.
That's what it feels like to stare at code weeks or days later sometimes.
Save yourself the hassle in the future and do it properly in the present.