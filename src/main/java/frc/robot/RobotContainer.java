// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;


import java.io.IOException;
import java.util.function.BiConsumer;

import org.photonvision.proto.Photon;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import frc.robot.Constants.FeederConstants;
import frc.robot.Constants.IntakeConstants;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.WristConstants;
import frc.robot.commands.AimWrist;
import frc.robot.commands.AlignToTagPhotonVision;
import frc.robot.commands.HoldWrist;
import frc.robot.commands.SetPosition;
import frc.robot.commands.SwerveDrive;
import frc.robot.commands.TeleopIntakeFeed;
import frc.robot.commands.TeleopPhotonTurret;
import frc.robot.commands.AutoCommands.AutoIntake;
import frc.robot.commands.AutoCommands.AutoShoot;
import frc.robot.subsystems.Wrist;
import frc.robot.sensors.PhotonVision;
import frc.robot.subsystems.Swerve;
import frc.robot.subsystems.*;

public class RobotContainer {

  /* Controllers */
  private final XboxController driver = new XboxController(OperatorConstants.driverPort);
  private final XboxController operator = new XboxController(OperatorConstants.operatorPort);

  /* Drive Controls */
  private final int translationAxis = XboxController.Axis.kLeftY.value;
  private final int strafeAxis = XboxController.Axis.kLeftX.value;
  private final int rotationAxis = XboxController.Axis.kRightX.value;

  /* Driver Buttons */
  private final JoystickButton zeroGyro = new JoystickButton(driver, XboxController.Button.kX.value);
  private final JoystickButton isEvading = new JoystickButton(driver, XboxController.Button.kRightBumper.value);
  private final JoystickButton isLocked = new JoystickButton(driver, XboxController.Button.kLeftBumper.value);
  private final JoystickButton alignToTag = new JoystickButton(driver, XboxController.Button.kA.value);
  private final POVButton stow = new POVButton(operator, 0);
  private final POVButton subwoofershoot = new POVButton(operator, 90);
  private final POVButton pickup = new POVButton(operator, 180);
  private final POVButton amp = new POVButton(operator, 270);
  private final JoystickButton aimWrist = new JoystickButton(operator, XboxController.Button.kY.value);

  private final JoystickButton rotateUp = new JoystickButton(operator, XboxController.Button.kLeftBumper.value);
  private final JoystickButton rotateDown = new JoystickButton(operator, XboxController.Button.kRightBumper.value);
  private final JoystickButton intakeButton = new JoystickButton(operator, XboxController.Button.kA.value);
  private final JoystickButton extakeButton = new JoystickButton(operator, XboxController.Button.kRightStick.value);
  private final JoystickButton ampButton = new JoystickButton(operator, XboxController.Button.kX.value);
  private final JoystickButton feed = new JoystickButton(operator, XboxController.Button.kB.value);
  private final JoystickButton climbOveride = new JoystickButton(operator, XboxController.Button.kLeftStick.value);

  /* Operator Buttons */

  

  /* Subsystems */

  private final Intake intake = new Intake();
  private final Climber climber = new Climber();
  private final Wrist wrist = new Wrist();
  private PhotonVision photonVision;
  private final Swerve swerve = new Swerve();
  private final Feeder feeder = new Feeder();



  /* Auton Chooser */
  private final SendableChooser<Command> autoChooser;

  public RobotContainer() {

    registerAutonCommands();
    
    try{
      photonVision = new PhotonVision();
    }    
    catch(IOException e){
      DriverStation.reportWarning("Unable to Initialize vision", e.getStackTrace());
    }
    

    swerve.setDefaultCommand(
      new SwerveDrive(
        swerve, 
        () -> driver.getRawAxis(translationAxis), 
        () -> driver.getRawAxis(strafeAxis), 
        () -> driver.getRawAxis(rotationAxis), 
        () -> false,
        () -> false,
        () -> isLocked.getAsBoolean()
      )
    );
    
    wrist.setDefaultCommand(
      new HoldWrist(wrist)
    ); 

    intake.setDefaultCommand(
      new TeleopIntakeFeed(
        intake, 
        feeder, 
        IntakeConstants.doNothing,
        wrist)
    );

    climber.setDefaultCommand(
      new RunCommand(
        () -> climber.setSpeed(-operator.getLeftY()), 
        climber)
    );

    /* Pathplanner Named Commands */
      //NamedCommands.registerCommand("Intake", new IntakeNote(m_intake, m_robotState));


    /* Autos */
    autoChooser = AutoBuilder.buildAutoChooser();
    SmartDashboard.putData("Auto Chooser", autoChooser);


    configureBindings();
  }

  private void registerAutonCommands(){
    NamedCommands.registerCommand(
      "Intake", 
      new TeleopIntakeFeed(
        intake, 
        feeder, 
        IntakeConstants.doIntake, 
        getWrist()));
  
    NamedCommands.registerCommand(
      "AutoIntake", 
      new AutoIntake(
        intake, 
        feeder, 
        wrist
      ));
    NamedCommands.registerCommand(
      "AutoShoot", 
      new AutoShoot(
        intake, 
        feeder, 
        wrist
      ));

    NamedCommands.registerCommand(
      "Shoot", 
      new TeleopIntakeFeed(
        intake, 
        feeder, 
        IntakeConstants.doShoot, 
        getWrist()));

    NamedCommands.registerCommand(
      "Pickup", 
      new SetPosition(
        wrist, 
        WristConstants.PICKUP));
    
    NamedCommands.registerCommand(
      "Stow", 
      new SetPosition(
        wrist, 
        WristConstants.STOW));

    NamedCommands.registerCommand(
      "Amp", 
      new SetPosition(
        wrist, 
        WristConstants.AMP));
  }

  private void configureBindings() {

    zeroGyro.onTrue(new InstantCommand(() -> swerve.zeroGyro()));

    stow.onTrue(new SetPosition(wrist, WristConstants.STOW));    

    pickup.onTrue(new SetPosition(wrist, WristConstants.PICKUP));

    subwoofershoot.onTrue(new SetPosition(wrist, WristConstants.SUBWOOFER));

    amp.onTrue(new SetPosition(wrist, WristConstants.AMP));   

    aimWrist.whileTrue(new AimWrist(photonVision, wrist));

    rotateUp.whileTrue(new RunCommand(() -> wrist.setSpeed(0.2), wrist));

    rotateDown.whileTrue(new RunCommand(() -> wrist.setSpeed(-0.1), wrist));

    intakeButton.whileTrue(new TeleopIntakeFeed(intake, feeder, IntakeConstants.doIntake, getWrist()));

    extakeButton.whileTrue(new TeleopIntakeFeed(intake, feeder, IntakeConstants.doExtale, getWrist()));

    ampButton.whileTrue(new TeleopIntakeFeed(intake, feeder, IntakeConstants.doAmp, wrist));
  
    feed.whileTrue(new TeleopIntakeFeed(intake, feeder, IntakeConstants.doShoot, getWrist()));

    climbOveride.whileTrue(new RunCommand(() -> climber.overideDown(), climber));
    

    alignToTag.whileTrue(
      new TeleopPhotonTurret(
        () -> driver.getRawAxis(translationAxis), 
        () -> driver.getRawAxis(strafeAxis), 
        swerve, 
        photonVision));


  }

  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }

  public void printValues(){
    /* Robot Position */
    //SmartDashboard.putData("Swerve", swerve);

    SmartDashboard.putString("Robot Pose2d", swerve.getPose().getTranslation().toString());
    SmartDashboard.putString("Robot Pose2d Rotation", swerve.getPose().getRotation().toString());
    SmartDashboard.putNumber("Robot Yaw", swerve.getYaw());
    SmartDashboard.putNumber("IntakeRPM", intake.getRPM());
    //SmartDashboard.putNumber("Robot Pitch", swerve.getPitch());
    //SmartDashboard.putNumber("Robot Roll", swerve.getRoll());
    // System.out.println(photonVision.hasAprilTag());
  }

  public Wrist getWrist(){
    return wrist;
  }
}
