package drgtools.dpscalc.modelPieces.temperature;

import drgtools.dpscalc.modelPieces.damage.DamageElements.TemperatureElement;

public class EnvironmentalTemperature {
    public enum TemperatureIntensity{Cold3, Cold2, Cold1, Heat1, Heat2, Heat3}

    private TemperatureElement tempElement;
    private double temperatureChangePerSec;

    public EnvironmentalTemperature(TemperatureIntensity envTempIntensity) {
        switch (envTempIntensity) {
            case Cold3: {
                tempElement = TemperatureElement.cold;
                temperatureChangePerSec = 20;
                break;
            }
            case Cold2: {
                tempElement = TemperatureElement.cold;
                temperatureChangePerSec = 12;
                break;
            }
            case Cold1: {
                tempElement = TemperatureElement.cold;
                temperatureChangePerSec = 2;
                break;
            }
            case Heat1: {
                tempElement = TemperatureElement.heat;
                temperatureChangePerSec = 2;
                break;
            }
            case Heat2: {
                tempElement = TemperatureElement.heat;
                temperatureChangePerSec = 12;
                break;
            }
            case Heat3: {
                tempElement = TemperatureElement.heat;
                temperatureChangePerSec = 20;
                break;
            }
            default: {
                tempElement = null;
                temperatureChangePerSec = 0;
                break;
            }
        }
    }

    public TemperatureElement getTempElement() {
        return tempElement;
    }
    public double getTemperatureChangePerSec() {
        return temperatureChangePerSec;
    }
}
