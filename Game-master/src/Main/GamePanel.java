package Main;

import Entity.Player;
import Input.KeyInputs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GamePanel extends JPanel {

    //Constantes para a largura e altura da tela.
    // (Estas constantes agora servem mais como tamanho PREFERIDO)
    public static final int LARGURA_TELA = 1920;
    public static final int ALTURA_TELA = 1080;

    Player player;

    public GamePanel() {
        // Define o tamanho preferido (o fullscreen vai tentar usar isso)
        this.setPreferredSize(new Dimension(LARGURA_TELA, ALTURA_TELA));
        this.setBackground(Color.BLACK);

    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (player == null) {
            return;
        }

        // ... (Seu código do timer está perfeito) ...
        double gameTime = player.getGameTime();
        String timeString = String.format("Tempo: %.2f", gameTime);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(timeString, 20, 30);

        // Desenha o jogador
        if (player.getSprite() != null) {
            g.drawImage(player.getSprite(), player.getPlayerX(), player.getPlayerY(),
                    128, 128, null);
        }

        // --- CORREÇÃO AQUI ---
        g.setColor(Color.GREEN);
        // Troque 'player.getPlataformaLargura()' por 'getWidth()'
        // Isso garante que a plataforma preencha TODA a largura da tela
        g.fillRect(player.getPlataformaX(), player.getPlataformaY(), getWidth(), player.getPlataformaAltura());
    }
}