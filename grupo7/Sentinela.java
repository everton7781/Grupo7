package grupo7;

import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.util.Utils;

public class Sentinela extends TeamRobot {

    private int direcao = 1;

    @Override
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        setMaxVelocity(2);

        while (true) {
            if (pertoDaParede()) {
                voltarParaOCentro();
            } else if (getDistanceRemaining() == 0) {
                direcao = -direcao;
                setTurnRight(Math.random() * 60 - 30);
                setAhead(direcao * (40 + Math.random() * 40));
            }

            if (getRadarTurnRemaining() == 0) {
                setTurnRadarRight(360);
            }

            execute();
        }
    }

    private boolean pertoDaParede() {
        double margem = 80;
        return getX() < margem || getX() > getBattleFieldWidth() - margem
            || getY() < margem || getY() > getBattleFieldHeight() - margem;
    }

    private void voltarParaOCentro() {
        double dx = getBattleFieldWidth() / 2 - getX();
        double dy = getBattleFieldHeight() / 2 - getY();
        double anguloCentro = Math.toDegrees(Math.atan2(dx, dy));
        setTurnRight(Utils.normalRelativeAngleDegrees(anguloCentro - getHeading()));
        setAhead(120);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {

    boolean companheiro = isTeammate(event.getName());

    out.println("Detectado: " + event.getName()
            + " | Companheiro: " + companheiro);

    if (companheiro) {
        return;
    }

    out.println("Robô: " + event.getName());

    out.println("É aliado? " + isTeammate(event.getName()));

        double anguloAbsoluto = getHeading() + event.getBearing();

        setTurnRadarRight(Utils.normalRelativeAngleDegrees(anguloAbsoluto - getRadarHeading()) * 2);

        setTurnGunRight(Utils.normalRelativeAngleDegrees(anguloAbsoluto - getGunHeading()));

        double distancia = event.getDistance();
        double potencia = distancia < 200 ? 3 : (distancia < 500 ? 2 : 1);

        out.println("Inimigo: " + event.getName() + " | dist: " + (int) distancia
                + " | calor da arma: " + getGunHeat()
                + " | giro restante: " + (int) getGunTurnRemaining());

        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(potencia);
            out.println("ATIROU com potência " + potencia);
        }
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        direcao = -direcao;
        setBack(80);
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {

    if (isTeammate(event.getName())) {
        setBack(100);
        return;
    }

    direcao = -direcao;
    setBack(80);
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        out.println("Mensagem recebida: " + event.getMessage());
    }
}