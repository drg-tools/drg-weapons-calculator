package drgtools.dpscalc.modelPieces.temperature;

import drgtools.dpscalc.modelPieces.damage.DamageElements.temperatureElement;

public class EnvironmentalTemperature {
    public enum TemperatureIntensity{Cold3, Cold2, Cold1, Heat1, Heat2, Heat3}

    private temperatureElement tempElement;
    private double temperatureChangePerSec;

    public EnvironmentalTemperature(TemperatureIntensity envTempIntensity) {
        switch (envTempIntensity) {
            case Cold3: {
                tempElement = temperatureElement.cold;
                temperatureChangePerSec = 20;
                break;
            }
            case Cold2: {
                tempElement = temperatureElement.cold;
                temperatureChangePerSec = 12;
                break;
            }
            case Cold1: {
                tempElement = temperatureElement.cold;
                temperatureChangePerSec = 2;
                break;
            }
            case Heat1: {
                tempElement = temperatureElement.heat;
                temperatureChangePerSec = 2;
                break;
            }
            case Heat2: {
                tempElement = temperatureElement.heat;
                temperatureChangePerSec = 12;
                break;
            }
            case Heat3: {
                tempElement = temperatureElement.heat;
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

    public temperatureElement getTempElement() {
        return tempElement;
    }
    public double getTemperatureChangePerSec() {
        return temperatureChangePerSec;
    }
}
