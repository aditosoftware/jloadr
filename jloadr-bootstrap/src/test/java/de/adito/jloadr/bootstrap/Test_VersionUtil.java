package de.adito.jloadr.bootstrap;

import org.junit.*;

/**
 * @author m.schindlbeck, 12.03.19
 */
public class Test_VersionUtil
{
  @Test
  public void testCompareVersions()
  {
    Assert.assertFalse(VersionUtil.validateJavaVersion("1.0", "1.2"));
    Assert.assertFalse(VersionUtil.validateJavaVersion("1.0", "9"));
    Assert.assertFalse(VersionUtil.validateJavaVersion("1.4", "1.5"));
    Assert.assertFalse(VersionUtil.validateJavaVersion("1.3", "1.6"));
    Assert.assertFalse(VersionUtil.validateJavaVersion("9", "10"));
    Assert.assertFalse(VersionUtil.validateJavaVersion("9", "11"));

    Assert.assertTrue(VersionUtil.validateJavaVersion("1.2", "1.0"));
    Assert.assertTrue(VersionUtil.validateJavaVersion("9", "1.0"));
    Assert.assertTrue(VersionUtil.validateJavaVersion("1.5", "1.4"));
    Assert.assertTrue(VersionUtil.validateJavaVersion("1.6", "1.3"));
    Assert.assertTrue(VersionUtil.validateJavaVersion("10", "9"));
    Assert.assertTrue(VersionUtil.validateJavaVersion("11", "9"));

  }

  @Test(expected = RuntimeException.class)
  public void testFormatFirstNumber()
  {
    VersionUtil.validateJavaVersion("10", "5.1");
  }

  @Test(expected = RuntimeException.class)
  public void testFormatFirstNumberNegative()
  {
    VersionUtil.validateJavaVersion("10", "-5.1");
  }

  @Test(expected = RuntimeException.class)
  public void testFormatFirstNumberZero()
  {
    VersionUtil.validateJavaVersion("10", "0");
  }

  @Test(expected = RuntimeException.class)
  public void testFormatNoInteger()
  {
    VersionUtil.validateJavaVersion("10", "NoInt");
  }

  @Test
  public void testFormatNull()
  {
    VersionUtil.validateJavaVersion("10", null);
  }

  @Test
  public void testCorrectFormat()
  {
    VersionUtil.validateJavaVersion("10", "9");
  }

  @Test
  public void testCorrectFormat2()
  {
    VersionUtil.validateJavaVersion("10", "1.8");
  }
}
