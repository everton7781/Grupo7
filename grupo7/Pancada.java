package grupo7;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.util.Utils;
import java.io.IOException;

public class Pancada extends TeamRobot {
    private int direcao = 1;
    private String ultimoInimigo = "";
    private double ultimaDistancia = 0;
    private double ultimaVelocidade = 0;
    private double ultimaDirecao = 0;
    private double inimigoX;
    private double inimigoY;
    private double velocidadeX;
    private double velocidadeY;
    private double direcaoAnterior = -1;
    private boolean mudouDirecao = false;

    @Override
    public void run() {
        try {
            broadcastMessage("Pancada online!");
        } catch (IOException e) {
            out.println("Erro ao enviar mensagem!");
        }

        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setMaxVelocity(3);

        while (true) {
            if (pertoDaParede()) {
                voltarParaOCentro();
            } else if (getDistanceRemaining() == 0) {
                direcao = Math.random() < 0.5 ? 1 : -1;
                setTurnRight(Math.random() * 120 - 60);
                setAhead(direcao * (60 + Math.random() * 80));
            }

            if (getRadarTurnRemaining() == 0) {
                setTurnRadarRight(Double.POSITIVE_INFINITY);
            }

            execute();
        }
    }

    private boolean pertoDaParede() {
        double margem = 60;
        return getX() < margem || getX() > getBattleFieldWidth() - margem
            || getY() < margem || getY() > getBattleFieldHeight() - margem;
    }

    private void voltarParaOCentro() {
        double dx = getBattleFieldWidth() / 2 - getX();
        double dy = getBattleFieldHeight() / 2 - getY();
        double anguloCentro = Math.toDegrees(Math.atan2(dx, dy));
        setTurnRight(Utils.normalRelativeAngleDegrees(anguloCentro - getHeading()));
        setAhead(150);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {

    if (isTeammate(event.getName())) {
        return;
    }

    double anguloAbsoluto = getHeading() + event.getBearing();

    setTurnRadarRight(
        Utils.normalRelativeAngleDegrees(
            anguloAbsoluto - getRadarHeading()
        ) * 2
    );

    // Posição atual do inimigo
    double anguloRadiano = Math.toRadians(anguloAbsoluto);

    inimigoX = getX() + Math.sin(anguloRadiano) * event.getDistance();
    inimigoY = getY() + Math.cos(anguloRadiano) * event.getDistance();

    // Velocidade do inimigo
    double direcaoRadiana = Math.toRadians(event.getHeading());

    velocidadeX = Math.sin(direcaoRadiana) * event.getVelocity();
    velocidadeY = Math.cos(direcaoRadiana) * event.getVelocity();

    // Identificação do inimigo
    if (!ultimoInimigo.equals(event.getName())) {
        ultimoInimigo = event.getName();
        direcaoAnterior = -1;
        mudouDirecao = false;

        out.println("NOVO INIMIGO: " + ultimoInimigo);
    }

    ultimaDistancia = event.getDistance();
    ultimaVelocidade = event.getVelocity();
    ultimaDirecao = event.getHeading();
    out.println("Direção atual do inimigo: " + (int) ultimaDirecao);

    out.println("ALVO: " + ultimoInimigo
        + " | Distância: " + (int) ultimaDistancia
        + " | Velocidade: " + ultimaVelocidade);

    // Verificação da mudança de direção
    if (direcaoAnterior != -1) {
        mudouDirecao = Math.abs(
            Utils.normalRelativeAngleDegrees(
                event.getHeading() - direcaoAnterior
            )
        ) > 10;
    }

    direcaoAnterior = event.getHeading();
    if (mudouDirecao) {
    out.println("INIMIGO MUDOU DE DIRECAO!");
    }

    // Potência do disparo
    double distancia = event.getDistance();
    double potencia;

    if (distancia < 200) {
        potencia = 3;
    } else if (distancia < 400) {
        potencia = 2.5;
    } else {
        potencia = 1.5;
    }

    if (getEnergy() < 30) {
        potencia = Math.min(potencia, 2);
    }

    // Previsão da posição futura
    double velocidadeProjetil = 20 - 3 * potencia;
    double tempo = distancia / velocidadeProjetil;

    double futuroX = inimigoX + velocidadeX * tempo;
    double futuroY = inimigoY + velocidadeY * tempo;

    // Ajuste da mira
    double anguloFuturo = Math.toDegrees(
        Math.atan2(futuroX - getX(), futuroY - getY())
    );

    double ajusteMira = Utils.normalRelativeAngleDegrees(
        anguloFuturo - getGunHeading()
    );

    setTurnGunRight(ajusteMira);

    // Disparo
    if (getGunHeat() == 0 &&
        Math.abs(getGunTurnRemaining()) < 2) {

        setFire(potencia);
    }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        setTurnRight(Utils.normalRelativeAngleDegrees(90 - (getHeading() - event.getHeading())));
        setAhead(direcao * 120);
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        direcao = -direcao;
        setBack(100);
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
}