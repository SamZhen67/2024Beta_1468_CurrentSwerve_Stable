package frc.robot.commands;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;

import static frc.robot.ConstantsMechanisms.ElbowConstants.*;

import frc.robot.ConstantsMechanisms.ElbowConstants;
import frc.robot.subsystems.ElbowSubsystem;

import com.revrobotics.CANSparkMax;

import com.revrobotics.SparkPIDController;

public class ElbowPIDCmdAT extends Command {
    private final ElbowSubsystem elbowSubsystem;
    private final SparkPIDController pidController;
    private  double setpoint;
//    private double maxVel;
//    private double maxAcc;
    private double tolerance;
    private double elbowMaxVel;
    private double elbowMaxAcc;

    public ElbowPIDCmdAT(ElbowSubsystem elbowSubsystem, double setpoint, double tolerance) {
        this.elbowSubsystem = elbowSubsystem;
        this.pidController = elbowSubsystem.getIntegratedSparkPID();

        this.pidController.setP(kP); 
        this.pidController.setI(kI);
        this.pidController.setD(kD);
        this.pidController.setFF(kFF);
        this.pidController.setOutputRange(kMinOutput, kMaxOutput);
        this.setpoint = setpoint;
        this.tolerance = tolerance;
//        this.maxVel = maxVel;
//        this.maxAcc = maxAcc;
        
        int smartMotionSlot = 0;
       this.pidController.setSmartMotionMinOutputVelocity(kMinVel, smartMotionSlot);
       this.pidController.setSmartMotionAllowedClosedLoopError(kAllowedErr, smartMotionSlot);
       this.pidController.setSmartMotionMaxVelocity(ElbowConstants.kMaxVelDown, smartMotionSlot);
       this.pidController.setSmartMotionMaxAccel(ElbowConstants.kMaxAccDown, smartMotionSlot);

        addRequirements(elbowSubsystem);
    }
    



    @Override
    public void initialize() {
 
        //       System.out.println("elbowPIDCmd started!");
 //       pidController.reset();

 
    if(setpoint > elbowSubsystem.getEncoderDegrees()) 
          {elbowMaxVel = ElbowConstants.kMaxVelUp; elbowMaxAcc = ElbowConstants.kMaxAccUp;}
    else  {elbowMaxVel = ElbowConstants.kMaxVelDown; elbowMaxAcc = ElbowConstants.kMaxAccDown;}

    this.pidController.setSmartMotionMaxVelocity(elbowMaxVel, smartMotionSlot);
    this.pidController.setSmartMotionMaxAccel(elbowMaxAcc, smartMotionSlot);



    }

    @Override
    public void execute() {

        NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
        NetworkTableEntry tv = table.getEntry("tv");
        NetworkTableEntry tz = table.getEntry("tz");
        double valid = tv.getDouble(0.0);
        double zDistanceInInches = tz.getDouble(0.0);

        if (valid == 1.0) { // Execute only if Limelight sees valid target
            setpoint = .46875 * zDistanceInInches - 9.0625;
        }

        pidController.setReference(setpoint, CANSparkMax.ControlType.kSmartMotion);
        SmartDashboard.putNumber("Elbow Setpoint", setpoint);
        SmartDashboard.putNumber("Elbow Tolerance", this.tolerance);

    }

    @Override
    public void end(boolean interrupted) {
        elbowSubsystem.setMotor(0);
//        System.out.println("elbowPIDCmd ended!");
    }

    @Override
    public boolean isFinished() {
        if (tolerance == 0) return false;       // hold elevator at cmded position until another command moves it
        else return (Math.abs((elbowSubsystem.getEncoderDegrees() - setpoint)) < this.tolerance);   // else if within tolerance end Command 
    }
}

