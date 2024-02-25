// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.LEDs;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class LEDs extends SubsystemBase {
  /** Creates a new LEDs. */
  private AddressableLED m_led;
  private AddressableLEDBuffer m_ledBuffer;

  double m_rainbowFirstPixelHue = 0;
  int count = 0;
  boolean toggled = true;

  /* creates all the different led strips and panels
   * the start of one is the end of another */
  private Panel leftPanel = new Panel(0, LEDConstants.panelWidth, LEDConstants.panelHeight);
  private Panel rightPanel = new Panel(leftPanel.getEnd(), LEDConstants.panelWidth, LEDConstants.panelHeight);
  
  private Strip frontGlow = new Strip(rightPanel.getEnd(), LEDConstants.frontUnderGlowLength);
  private Strip backGlow = new Strip(frontGlow.getEnd(), LEDConstants.backUnderGlowLength);
  private Strip leftGlow = new Strip(backGlow.getEnd(), LEDConstants.leftUnderGlowLength);
  private Strip rightGlow = new Strip(leftGlow.getEnd(), LEDConstants.rightUnderGlowLength);

  private Strip intakeGlow = new Strip(rightGlow.getEnd(), LEDConstants.intakeGlow);

  Strip[] underGlow = new Strip[] {
      frontGlow,
      backGlow,
      leftGlow,
      rightGlow
  };

  public LEDs() {
    //PWM port 1 on the Rio
    m_led = new AddressableLED(1);

    //sets the length of the LEDs
    m_ledBuffer = new AddressableLEDBuffer(getLength());

    setUpLight();

    count = 0;
  }

  private void setUpLight(){
    //defines the length of the leds
    //setting the length is expensive so only call it once
    m_led.setLength(m_ledBuffer.getLength());

    //sets the LED data and starts the signal
    m_led.setData(m_ledBuffer);
    m_led.start();
  }

  /*
   * returns the length of the entire led system
   * intakeGlow is the last strip of leds
   * the end of intakeGlow is the total length of leds
   */
  public int getLength(){
    return intakeGlow.getEnd();
  }

  public int[] getAllLEDS(){
    return new int[]{0, getLength()};
  }

  public int[] getStripsRange(){
    return new int[]{getUnderglowRange()[0], getIntakeRange()[1]};
  }

  public int[] getUnderglowRange(){
    return new int[]{underGlow[0].getStart(), underGlow[underGlow.length-1].getEnd()};
  }

  public int[] getPanelsRange(){
    return new int[]{getLeftPanelRange()[0], getRightPanelRange()[1]};
  }

  public int[] getLeftPanelRange(){
    return new int[]{leftPanel.getStart(), leftPanel.getEnd()};
  }

  public int[] getRightPanelRange(){
    return new int[]{rightPanel.getStart(), rightPanel.getEnd()};
  }

  public int[] getIntakeRange(){
    return new int[]{intakeGlow.getStart(), intakeGlow.getEnd()};
  }

  public Panel getRightPanel(){
    return rightPanel;
  }

  public Panel getLeftPanel(){
    return leftPanel;
  }

  public void setAllColor(int[] color){
    setAllStrips(color);
    setAllPanelColor(color);
  }

  public void setAllStrips(int[] color){
    setUnderGlowColor(color);
    setStripColor(intakeGlow, color);
  }

  public void setAllPanelColor(int[] color){
    setPanelColor(leftPanel, color);
    setPanelColor(rightPanel, color);
  }

  public void setColor(int[] startEnd, int[] color){
    for (int i = startEnd[0]; i < startEnd[1]; i++){
      m_ledBuffer.setRGB(i, color[0], color[1], color[2]); 
    }
  }

  public void setUnderGlowColor(int[] color){
    for (Strip strip : underGlow){
      setStripColor(strip, color);
    }
  }

  private void setPanelColor(Panel panel, int[] color){
    m_led.setData(panel.setAllColor(m_ledBuffer, color));
  }

  private void setStripColor(Strip strip, int[] color){
    m_led.setData(strip.setAllColor(m_ledBuffer, color));
  }

  public void setAllOff(){
    setAllColor(new int[]{0, 0, 0});
  }

  public void setRainbow(int[] startEnd){
    for (int i = startEnd[0]; i < startEnd[1]; i++){
      final int hue = ((int)(m_rainbowFirstPixelHue) + (i * 180 / m_ledBuffer.getLength())) % 180;

      m_ledBuffer.setHSV(i, hue, 255, 64);
    }
    m_rainbowFirstPixelHue += 0.5;

    m_rainbowFirstPixelHue %= 180;
    m_led.setData(m_ledBuffer);
  }

  public void setShape(Panel panel, int[][][] shape){
    m_led.setData(panel.setShape(m_ledBuffer, shape));
  }

  public int[][][] getMirrored(int[][][] state){
    int[][][] leds = state;
    for(int row = 0; row < leds.length; row++){
      for(int col = 0; col < leds[row].length / 2; col++) {
          int[] temp = leds[row][col];
          leds[row][col] = leds[row][leds[row].length - col - 1];
          leds[row][leds[row].length - col - 1] = temp;
      }
    } 
    return leds;
  }

  public void togglePanel(Panel firstPanel, Panel secondPanel, int[][][]state){
    count++;
    if (count == 1){
      if (toggled){
        firstPanel.setShape(m_ledBuffer, state);
        setPanelColor(secondPanel, LEDConstants.n);
        toggled = false;
      } else {
        secondPanel.setShape(m_ledBuffer, state);
        setPanelColor(firstPanel, LEDConstants.n);
        toggled = true;
      }
      state = getMirrored(state);
    }  else if (count > 50){
      count = 0;
    }
  }


}
