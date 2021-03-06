package team6458;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import team6458.cmd.AutoDeliverCommand;
import team6458.cmd.AutoDeliverCommand.AllianceSide;
import team6458.cmd.DriveStraightCommand;
import team6458.cmd.GyroCalibrationCommand;
import team6458.cmd.RotateCommand;
import team6458.util.ValueGradient;
import team6458.subsystem.Drivetrain;
import team6458.subsystem.Ramp;
import team6458.subsystem.Sensors;
import team6458.util.DashboardKeys;
import team6458.util.PlateAssignment;
import team6458.util.exception.GetBeforeInitException;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static team6458.util.DashboardKeys.CHOOSER_AUTONOMOUS;
import static team6458.util.DashboardKeys.CMD_GYRO_CALIBRATE;
import static team6458.util.DashboardKeys.CMD_RESET_ENCODERS;
import static team6458.util.DashboardKeys.GYROSCOPE;
import static team6458.util.DashboardKeys.LEFT_ENCODER;
import static team6458.util.DashboardKeys.RIGHT_ENCODER;
import static team6458.util.DashboardKeys.SQUARE_INPUTS;
import static team6458.util.DashboardKeys.TANK_CONTROLS;

/**
 * The main robot class.
 */
public final class SemiRobot extends TimedRobot {

    private static final Logger LOGGER = Logger.getLogger(SemiRobot.class.getName());
    // SendableChoosers
    private final SendableChooser<Command> debugCommands = new SendableChooser<>();
    private final SendableChooser<Supplier<Command>> autoChooser = new SendableChooser<>();
    // Plate assignment
    private PlateAssignment plateAssignment = PlateAssignment.ALL_INVALID;
    // Operator control
    private OperatorControl opControl;
    // Subsystems
    private Drivetrain drivetrain;
    private Sensors sensors;
    private Ramp ramp;

    @Override
    public void robotInit() {
        LOGGER.log(Level.INFO,
                "\n==========================\nStarting initialization...\n==========================\n");

        Scheduler.getInstance().enable();

        opControl = new OperatorControl(this);

        // Start up the subsystems
        {
            drivetrain = new Drivetrain(this);
            ramp = new Ramp(this);
            // Sensors should be last: a gyroscope will be calibrated for around 5 seconds blocking the thread
            sensors = new Sensors(this);
        }

        // Write one-time values to the SmartDashboard/Shuffleboard so they can be displayed as widgets
        // Use the DashboardKeys class for string IDs
        // All other continuously updated values are updated in robotPeriodic
        {
            // One-time init so that they appear first
            updateSmartDashboardPeriodic();

            SmartDashboard.putBoolean(TANK_CONTROLS, SmartDashboard.getBoolean(TANK_CONTROLS, false));
            SmartDashboard.putBoolean(SQUARE_INPUTS, SmartDashboard.getBoolean(SQUARE_INPUTS, true));

            // Autonomous command selection
            {
                final ValueGradient gradient = RotateCommand.DEFAULT_GRADIENT;
                final double throttle = 0.6;
                final double lastStretchThrottle = 0.8;

                autoChooser.addDefault("SWITCH DELIVERY - Centre position",
                        () -> new AutoDeliverCommand(this, AllianceSide.CENTRE,
                                getPlateAssignment().getNearest(), true,
                                throttle, lastStretchThrottle, gradient));
                autoChooser.addObject("SWITCH DELIVERY - Left position",
                        () -> new AutoDeliverCommand(this, AllianceSide.LEFT,
                                getPlateAssignment().getNearest(), true,
                                throttle, lastStretchThrottle, gradient));
                autoChooser.addObject("SWITCH DELIVERY - Right position",
                        () -> new AutoDeliverCommand(this, AllianceSide.RIGHT,
                                getPlateAssignment().getNearest(), true,
                                throttle, lastStretchThrottle, gradient));

                // Simply pretend you're on the other side to "avoid" the switch
                autoChooser.addObject("AVOID SWITCH - Left position",
                        () -> new CommandGroup() {
                            {
                                addSequential(new AutoDeliverCommand(SemiRobot.this, AllianceSide.RIGHT,
                                        PlateAssignment.PlateSide.RIGHT, false,
                                        throttle, throttle, gradient));
                                addSequential(new RotateCommand(SemiRobot.this, 165.0));
                            }
                        });
                autoChooser.addObject("AVOID SWITCH - Right position",
                        () -> new CommandGroup() {
                            {
                                addSequential(new AutoDeliverCommand(SemiRobot.this, AllianceSide.LEFT,
                                        PlateAssignment.PlateSide.LEFT, false,
                                        throttle, throttle, gradient));
                                addSequential(new RotateCommand(SemiRobot.this, 165.0));
                            }
                        });

                autoChooser.addObject("DO NOT MOVE - NO AUTONOMOUS", InstantCommand::new);

                SmartDashboard.putData(CHOOSER_AUTONOMOUS, autoChooser);
            }

            // Self-updating sendables, like the gyroscope and encoders
            SmartDashboard.putData(GYROSCOPE, getSensors().gyro);
            SmartDashboard.putData(LEFT_ENCODER, getDrivetrain().leftEncoder);
            SmartDashboard.putData(RIGHT_ENCODER, getDrivetrain().rightEncoder);

            // Commands
            SmartDashboard.putData(CMD_GYRO_CALIBRATE, new GyroCalibrationCommand(this));
            SmartDashboard.putData(CMD_RESET_ENCODERS, new InstantCommand() {

                {
                    setRunWhenDisabled(true);
                }

                @Override
                protected void execute() {
                    super.execute();
                    getDrivetrain().resetEncoders();
                }
            });

            // TESTS -----------------------------------------------------------------------
            debugCommands.addDefault("None", new InstantCommand());

            // RotateCommand tests
            final int[] angles = {20, 45, 50, 90, 180, 360};
            Arrays.stream(angles).forEach(d -> {
                debugCommands.addObject("Turn -" + d + " deg (LEFT)", new RotateCommand(this, -d));
                debugCommands.addObject("Turn +" + d + " deg (RIGHT)", new RotateCommand(this, d));
            });

            // Encoder tests
            final double[] distances = {0.5, 1.0, 2.0, 3.0};
            for (double distance : distances) {
                debugCommands.addObject("Drive +" + distance + " m", new DriveStraightCommand(this, distance, 0.35));
                debugCommands.addObject("Drive -" + distance + " m", new DriveStraightCommand(this, -distance, 0.35));
            }

            debugCommands.addObject("Turn +360 deg at 0.2 speed",
                    new RotateCommand(this, 360, new ValueGradient(0.2, 0.2, 20.0, 10.0)));

            SmartDashboard.putData("DEBUG (Enabling Test Mode will run sel. command)", debugCommands);
        }

        LOGGER.log(Level.INFO,
                "\n==============================\nRobot initialization complete.\n==============================\n");
    }

    @Override
    public void disabledInit() {
        // Disables any trailing cmds
        Scheduler.getInstance().removeAll();
    }

    @Override
    public void autonomousInit() {
        updatePlateAssignmentFromFMS();

        // Enables commands to be run
        Scheduler.getInstance().removeAll();

        // Choose autonomous program
        final Supplier<Command> supplier = autoChooser.getSelected();
        if (supplier == null) {
            LOGGER.log(Level.WARNING, "Null auto command");
        } else {
            final Command cmd = supplier.get();
            LOGGER.log(Level.INFO, "Running auto command: " + cmd.getName());
            cmd.start();
        }
    }

    @Override
    public void teleopInit() {
    }

    @Override
    public void testInit() {
        final Command cmd = debugCommands.getSelected();
        Scheduler.getInstance().enable();
        Scheduler.getInstance().removeAll();
        if (cmd != null) {
            cmd.start();
        }
    }

    // ---------------------------------------------------------------------------

    @Override
    public void robotPeriodic() {
        getOperatorControl().periodicUpdate();

        // Run the scheduler. This does nothing if it is disabled.
        Scheduler.getInstance().run();

        // Update SmartDashboard
        updateSmartDashboardPeriodic();
    }

    @Override
    public void disabledPeriodic() {
        updatePlateAssignmentFromFMS();
    }

    @Override
    public void autonomousPeriodic() {
    }

    @Override
    public void teleopPeriodic() {
    }

    @Override
    public void testPeriodic() {
    }

    // Subsystem getters

    public Drivetrain getDrivetrain() {
        if (drivetrain == null) {
            throw new GetBeforeInitException("drivetrain");
        }
        return drivetrain;
    }

    public Sensors getSensors() {
        if (sensors == null) {
            throw new GetBeforeInitException("sensors");
        }
        return sensors;
    }

    public Ramp getRamp() {
        if (ramp == null) {
            throw new GetBeforeInitException("ramp");
        }
        return ramp;
    }

    // Private methods

    /**
     * Update certain values on the SmartDashboard.
     */
    private void updateSmartDashboardPeriodic() {
    }

    /**
     * Internal method that updates the plate assignment from the Field Management System.
     */
    private void updatePlateAssignmentFromFMS() {
        final boolean isFMSAttached = DriverStation.getInstance().isFMSAttached();
        final String fmsData = DriverStation.getInstance().getGameSpecificMessage();
        final PlateAssignment oldAssignment = plateAssignment;
        if (fmsData == null || fmsData.equals("")) {
            /*
            Note: a reference equality check is valid here because ALL_INVALID is the only possible "unknown"
            constant that is settable in these conditional branches
             */
            if (plateAssignment != PlateAssignment.ALL_INVALID) {
                LOGGER.log(Level.INFO,
                        "Plate assignment set to ALL_INVALID, got null, was " + plateAssignment + " (FMS attached: " +
                                isFMSAttached + ")");
                plateAssignment = PlateAssignment.ALL_INVALID;
            }
        } else {
            if (!plateAssignment.toString().equals(fmsData)) {
                LOGGER.log(Level.INFO,
                        String.format("Plate assignment set to %s, was %s", fmsData, plateAssignment.toString()));
                plateAssignment = PlateAssignment.fromString(fmsData);
            }
        }

        if (plateAssignment != oldAssignment) {
            // Update SmartDashboard data
            SmartDashboard.putString(DashboardKeys.FMS_GAME_DATA, getPlateAssignment().toString());
        }
    }

    // Getters and setters

    public OperatorControl getOperatorControl() {
        if (opControl == null) {
            throw new GetBeforeInitException("operator control");
        }
        return opControl;
    }

    /**
     * @return The non-null plate assignment
     */
    public PlateAssignment getPlateAssignment() {
        return plateAssignment;
    }
}
