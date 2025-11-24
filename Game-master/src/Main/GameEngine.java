package Main;

import Entity.Player;
import Input.KeyInputs;

import java.awt.*;
import java.io.Serial;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameEngine extends Canvas implements Runnable {

    private GameWindow gameWindow;
    private GamePanel gamePanel;
    private Player player;
    private KeyInputs keyInputs;


    private long gameStartTime; //MARCOS
    private double elapsedGameTimeSeconds; //MARCOS

    @Serial
    private static final long serialVersionUID = 1L;

    //Controle Main.Game Loop
    private Thread gameThread;
    private volatile boolean running = false;

    //Controle FPS
    public static final int FPS = 60;

    //Intervalo de tempo para cada atualização em segundos.
    public static final float SECONDS_PER_UPDATE = 1.0f / FPS;

    private int playerID;

    public GameEngine(GamePanel gamePanel, int playerID) {
        this.gamePanel = gamePanel;
        // No construtor da GameEngine:
        this.playerID = playerID; // Armazena o ID do jogador

        // Inicializa os componentes do jogo
        this.player = new Player(this, gamePanel);
        this.keyInputs = new KeyInputs(gamePanel);
        // Configura os links
        gamePanel.setPlayer(player);
        player.setKeyInputs(keyInputs);

        // Configura o input no painel
        gamePanel.addKeyListener(keyInputs);
        gamePanel.setFocusable(true);

        // Marca o tempo de início
        gameStartTime = System.nanoTime();
    }

    protected void updatePlayer() {
        // Também trava o player se o jogo acabou
        if (player != null && !gamePanel.isGameOver()) {
            player.update();
        }
    }

    private void update(float secondsPerUpdate) {
        // Se o jogo acabou, NÃO atualiza nada
        if (gamePanel.isGameOver()) {
            return;
        }

        if (gamePanel != null) {
            gamePanel.update();
        }
    }

    protected void render(float interpolation) {
    }

    // Inicia o game loop em uma nova thread
    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Para o game loop de forma segura
    public synchronized void stop() {
        if (!running) return;
        running = false;

        // 1. Calcula a distância final (igual ao HUD: worldX / 20)
        int distanciaFinal = 0;
        if (player != null) {
            distanciaFinal = player.getWorldX() / 20;
        }

        // 2. Salva ID, Distância e Tempo
        Util.DatabaseConnection.saveScore(this.playerID, distanciaFinal, elapsedGameTimeSeconds);

        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Usa o tempo de alta complexidade do sistema (nanosegundos)
        long lastTime = System.nanoTime();
        double timeAccumulator = 0.0;

        //Contadores para debugar
        long timer = System.currentTimeMillis();
        int frames = 0;
        int updates = 0;

        while (running) {
            long now = System.nanoTime();
            // Calcula o tempo que passou desde a última verificação em segundos
            double elapsedTime = (now - lastTime) / 1000000000.0;
            lastTime = now;

            // --- MODIFICAÇÃO AQUI ---
            // Só adiciona tempo ao cronômetro se o jogo NÃO estiver travado no Game Over
            if (!gamePanel.isGameOver()) {
                elapsedGameTimeSeconds += elapsedTime;
            }
            //Adiciona o tempo decorrido ao acumulador
            timeAccumulator += elapsedTime;


            // Lógica de atulização usando o Fixed Timestep
            while (timeAccumulator >= SECONDS_PER_UPDATE) {
                update(SECONDS_PER_UPDATE);
                updatePlayer();
                timeAccumulator -= SECONDS_PER_UPDATE;
                updates++; // Para debugar o FPS
            }

            // Lógica de Renderização com Variable Timestep com Interpolação
            final float interpolation = (float) (timeAccumulator / SECONDS_PER_UPDATE);
            render(interpolation);
            gamePanel.repaint();
            frames++;

            // Exibe o FPS e UPS
            if (System.currentTimeMillis() - timer > 1000) {
                System.out.printf("UPS: %d, FPS: %d%n, Tempo: %.2f s%n", updates, frames, elapsedGameTimeSeconds);
                updates = 0;
                frames = 0;
                timer += 1000;
            }

            // Sistema de pausa para máquinas muito fortes de hardware
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    Graphics2D g2d = (Graphics2D) this.getGraphics();

    public Player getPlayer() {
        return this.player;
    }

    public double getElapsedGameTimeSeconds() {
        return elapsedGameTimeSeconds;
    }

    // Método novo para zerar o tempo manualmente
    public void resetTimer() {
        this.elapsedGameTimeSeconds = 0;
    }

}
