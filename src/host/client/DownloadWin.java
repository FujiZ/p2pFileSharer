package host.client;

import javax.swing.*;
import java.awt.*;

public class DownloadWin extends JFrame {
    public DownloadWin(String filename){
        super("Downloading "+filename);
        this.filename=filename;

        //Create and set up the content pane.
        progress = new DownloadProgress();
        progress.setOpaque(true); //content panes must be opaque
        setContentPane(progress);

        //Display the window.
        setSize(300,250);
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        setLocation((screen_width - getWidth()) / 2,
                (screen_height - getHeight()) / 2);
        setVisible(true);
    }

    public void progressChange(int value){
        progress.change(value);
    }

    public void progressDone(){
        progress.done();
        JOptionPane.showMessageDialog(this,filename+" download complete!");
        dispose();
    }

    private String filename;
    private DownloadProgress progress;

}

class DownloadProgress extends JPanel{

    private JProgressBar progressBar;
    private JTextArea taskOutput;

    public DownloadProgress() {
        super(new BorderLayout());

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);

        JPanel panel = new JPanel();
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

    }

    /**
     * Invoked when task's progress property changes.
     */
    public void change(int progress) {
        progressBar.setValue(progress);
        taskOutput.append(String.format("Completed %d%% of download.\n",progress));
    }

    public void done(){
        taskOutput.append("Done!\n");
    }

}
