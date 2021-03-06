package lostvaults.client;
/**
 * GUI.java
 * @Author Felix Färsjö, Jimmy Holm, Fredrik Larsson, Anna Nilsson, Philip Åkerfeldt
 * @Version 1.0
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUI {

	/*******************************************
	 * * Declarations * *
	 *******************************************/
	String name;
	String passwordstr;

	int screenWidth;
	int screenHeight;

	JLabel dynamicInfoLabel = new JLabel("The Lost Vaults - Uneasy alliance ");
	JTextArea dynamicInfo = new JTextArea();
	JTextField commandInputField = new JTextField();

	JTextArea dungeonPlayers = new JTextArea();
	JTextArea roomPlayers = new JTextArea();
	JTextArea npcs = new JTextArea();
	JTextArea items = new JTextArea();
	JTextArea exits = new JTextArea();
	JLabel healthStats = new JLabel();
	JLabel combatStats = new JLabel();
	JLabel playersInDungeon = new JLabel(" Players In City: ");
	JLabel playersInRoom = new JLabel(" Players In Room: ");
	JLabel itemsInRoom = new JLabel(" Items In Room: ");
	JLabel npcsInRoom = new JLabel(" NPC In Room: ");
	JFrame window = new JFrame("The Lost Vaults");

	Color darkBackground = new Color(0x800000);
	Color lightBackground = new Color(0xDAC8A3);
	Color textColor = new Color(0x3D1515);
	Color lightTextColor = new Color(0xDE9D5C);
	Color mediumBackground = new Color(0xC2A366);
	Color darkMediumBackground = new Color(0xB58C3C);

	Font font = new Font("Serif", Font.BOLD + Font.ITALIC, 14);
	Font bigFont = new Font("Serif", Font.BOLD + Font.ITALIC, 15);

	/*******************************************
	 * * Windows in the GUI * *
	 *******************************************/
	public GUI() {
		// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		screenWidth = 1000; // screenSize.width;
		screenHeight = 600; // screenSize.height;
		window.setSize(screenWidth, screenHeight);
		// window.setUndecorated(true); //true -> no exit button
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		dynamicInfoLabel.setForeground(lightTextColor);
		dynamicInfoLabel.setFont(font);
		dynamicInfo.setEditable(false);
		dynamicInfo.setBackground(lightBackground);
		dynamicInfo
				.setBorder(BorderFactory.createLineBorder(darkBackground, 2));
		dynamicInfo.setFont(font);
		dynamicInfo.setForeground(textColor);
		dynamicInfo.setLineWrap(true);
		dynamicInfo.setWrapStyleWord(true);
		JScrollPane dynamicInfoScroll = new JScrollPane(dynamicInfo);
		dynamicInfoScroll.setBorder(BorderFactory.createEmptyBorder());
		dynamicInfoScroll
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel dynamicInfoPanel = new JPanel(new BorderLayout());
		dynamicInfoPanel.setBackground(darkBackground);
		dynamicInfoPanel.add(dynamicInfoScroll, BorderLayout.CENTER);
		dynamicInfoPanel.add(dynamicInfoLabel, BorderLayout.NORTH);
		/*******************************************
		 * * Static window in the GUI * *
		 *******************************************/
		healthStats.setText("HP: 0/0 Food: 0/0 Gold: 0     ");
		healthStats.setFont(font);
		healthStats.setForeground(lightTextColor);
		combatStats.setText("Attack: 0 Defense: 0 Speed 0     ");
		combatStats.setFont(font);
		combatStats.setForeground(lightTextColor);
		JPanel statsPanel = new JPanel(new BorderLayout());
		statsPanel.setBackground(darkBackground);
		statsPanel.add(healthStats, BorderLayout.NORTH);
		statsPanel.add(combatStats, BorderLayout.CENTER);

		JPanel dungeonPlayersPanel = createRightBox(dungeonPlayers,
				playersInDungeon, "Players in Dungeon: ");
		JPanel roomPlayersPanel = createRightBox(roomPlayers, playersInRoom,
				"Players in Room: ");
		JPanel npcsPanel = createRightBox(npcs, npcsInRoom, "NPCs in Room: ");
		JPanel itemsPanel = createRightBox(items, itemsInRoom,
				"Items in Room: ");

		exits.setFont(font);
		exits.setForeground(textColor);
		exits.setBackground(mediumBackground);
		exits.setEditable(false);
		JLabel exitsLabel = new JLabel("Exits in Room: ");
		exitsLabel.setForeground(textColor);
		exitsLabel.setFont(bigFont);
		JPanel exitsPanel = new JPanel(new BorderLayout());
		exitsPanel.setPreferredSize(new Dimension(0, 50));
		exitsPanel.setBackground(darkMediumBackground);
		exitsPanel.setBorder(BorderFactory.createLineBorder(darkBackground, 2));
		exitsPanel.add(exitsLabel, BorderLayout.NORTH);
		exitsPanel.add(exits, BorderLayout.CENTER);

		JPanel staticInfo = new JPanel(new GridLayout(0, 1));
		staticInfo.setBackground(darkBackground);
		staticInfo.add(dungeonPlayersPanel);
		staticInfo.add(roomPlayersPanel);
		staticInfo.add(npcsPanel);
		staticInfo.add(itemsPanel);

		JPanel rightPanel = new JPanel(new BorderLayout());
		// rightPanel.setPreferredSize(new Dimension(window.getSize().width / 3,
		// 0));
		rightPanel.setBackground(darkBackground);
		rightPanel.add(statsPanel, BorderLayout.NORTH);
		rightPanel.add(staticInfo, BorderLayout.CENTER);
		rightPanel.add(exitsPanel, BorderLayout.SOUTH);

		JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				dynamicInfoPanel, rightPanel);
		mainPanel.setResizeWeight(0.85);
		mainPanel.setBackground(darkBackground);
		mainPanel.setBorder(null);

		/*******************************************
		 * * Command box in the GUI * *
		 *******************************************/
		JLabel commandLabel = new JLabel("Command: ");
		commandLabel.setFont(new Font("Serif", Font.BOLD, 16));
		commandLabel.setForeground(lightTextColor);
		commandInputField.setBackground(mediumBackground);
		commandInputField.setBorder(BorderFactory.createLineBorder(
				darkBackground, 2));
		commandInputField.setFont(font);
		commandInputField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField source = (JTextField) (e.getSource());
				playGameCommunication.sendMessage(source.getText());
				source.setText("");
			}
		});

		JPanel command = new JPanel(new BorderLayout());
		command.setBackground(darkBackground);
		command.add(commandLabel, BorderLayout.WEST);
		command.add(commandInputField, BorderLayout.CENTER);

		window.add(command, BorderLayout.SOUTH);
		window.add(mainPanel, BorderLayout.CENTER);
		window.setLocationRelativeTo(null);
		window.setVisible(false);

		new LogInPopUp(window);
	}

	/*******************************************
	 * * Right window in the GUI * *
	 *******************************************/
	public JPanel createRightBox(JTextArea c, JLabel cLabel, String label) {
		c.setFont(font);
		c.setForeground(textColor);
		c.setBackground(mediumBackground);
		c.setEditable(false);
		JScrollPane cScroll = new JScrollPane(c);
		cScroll.setBorder(BorderFactory.createEmptyBorder());
		cLabel.setForeground(textColor);
		cLabel.setFont(bigFont);
		JPanel cPanel = new JPanel(new BorderLayout());
		cPanel.setBackground(darkMediumBackground);
		cPanel.setBorder(BorderFactory.createLineBorder(darkBackground, 2));
		cPanel.add(cLabel, BorderLayout.NORTH);
		cPanel.add(cScroll, BorderLayout.CENTER);
		return cPanel;
	}

	/*******************************************
	 * * Login in pop up box * *
	 *******************************************/

	public class LogInPopUp extends JDialog implements ActionListener,
			KeyListener {

		JFrame window;
		JButton button;
		JTextArea message = new JTextArea();

		JTextField userNameInput = new JTextField();
		JLabel userNameLabel = new JLabel("User name: ");
		JPanel userName = new JPanel(new BorderLayout());

		JPasswordField passwordInput = new JPasswordField();
		JLabel passwordLabel = new JLabel("Password: ");
		JPanel password = new JPanel(new BorderLayout());

		JTextField IPInput = new JTextField();
		JLabel IPLabel = new JLabel("IP: ");
		JPanel IP = new JPanel(new BorderLayout());

		Font font = new Font("Serif", Font.BOLD + Font.ITALIC, 14);
		Color darkBackground = new Color(0x800000);
		Color lightBackground = new Color(0xDAC8A3);
		Color textColor = new Color(0x3D1515);
		Color lightTextColor = new Color(0xDE9D5C);
		Color mediumBackground = new Color(0xC2A366);

		/*******************************************
		 * * Creation of the login window * *
		 *******************************************/
		public LogInPopUp(JFrame _window) {
			window = _window;

			// Setting up the fields
			button = new JButton("OK");
			button.addActionListener(this);
			button.setBackground(mediumBackground);
			button.setFont(font);
			button.setForeground(textColor);

			userNameLabel.setPreferredSize(new Dimension(150, 0));
			userNameInput.setBackground(mediumBackground);
			userNameInput.setFont(font);
			userNameInput.setForeground(textColor);
			userNameInput.addKeyListener(this);
			userName.setBackground(darkBackground);
			userNameLabel.setFont(font);
			userNameLabel.setForeground(lightTextColor);
			userName.setBorder(BorderFactory
					.createLineBorder(darkBackground, 1));
			userName.add(userNameInput, BorderLayout.CENTER);
			userName.add(userNameLabel, BorderLayout.WEST);

			passwordLabel.setPreferredSize(new Dimension(150, 0));
			passwordInput.setBackground(mediumBackground);
			passwordInput.setFont(font);
			passwordInput.setForeground(textColor);
			passwordInput.addKeyListener(this);
			password.setBackground(darkBackground);
			passwordLabel.setFont(font);
			passwordLabel.setForeground(lightTextColor);
			password.setBorder(BorderFactory
					.createLineBorder(darkBackground, 1));
			password.add(passwordInput, BorderLayout.CENTER);
			password.add(passwordLabel, BorderLayout.WEST);
			passwordInput.setText("");

			IPInput.setText("localhost"); // 127.0.0.1 Om man är på egen dator
			IPLabel.setPreferredSize(new Dimension(150, 0));
			IPInput.setBackground(mediumBackground);
			IPInput.setFont(font);
			IPInput.setForeground(textColor);
			IPInput.addKeyListener(this);
			IP.setBackground(darkBackground);
			IPLabel.setFont(font);
			IPLabel.setForeground(lightTextColor);
			IP.setBorder(BorderFactory.createLineBorder(darkBackground, 1));
			IP.add(IPInput, BorderLayout.CENTER);
			IP.add(IPLabel, BorderLayout.WEST);

			new BorderLayout();
			JPanel frame = new JPanel(new GridLayout(0, 1));
			frame.add(userName);
			frame.add(password);
			frame.add(IP);

			message.setText("\n");
			message.setFont(font);
			message.setForeground(textColor);
			message.setEditable(false);
			message.setBackground(mediumBackground);
			add(message, BorderLayout.NORTH);
			add(frame, BorderLayout.CENTER);
			add(button, BorderLayout.SOUTH);

			setSize(new Dimension(600, 180));

			setLocationRelativeTo(null);
			setVisible(true);
			userNameInput.requestFocus();
		}

		/*******************************************
		 * * Keyboard Handling Events * *
		 *******************************************/
		public void keyTyped(KeyEvent e) {
		}

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
				tryConnect();
		}

		/*******************************************
		 * * Action event for popup window * *
		 *******************************************/
		public void actionPerformed(ActionEvent e) {
			tryConnect();
		}

		private void tryConnect() {
			String user = userNameInput.getText();
			String pass = new String(passwordInput.getPassword());
			String ip = IPInput.getText();
			if (user.equals("")) {
				message.setText("You must enter a username\n");
			} else if (password.equals("")) {
				message.setText("You must enter a password\n");
			} else if (ip.equals("")) {
				// String pwd = passwordInput.getText(); //String ip =
				message.setText("You must enter an IP address.\n");
			} else {
				IPInput.getText();
				user = user.replace(" ", "");
				name = user;
				passwordstr = pass;
				dynamicInfoLabel
						.setText("The Lost Vaults - Uneasy Alliance --- "
								+ name);
				playGameCommunication.sendIP(ip);
				window.setVisible(true);
				commandInputField.requestFocus();
				dispose();
			}
		}
	}

	/*******************************************
	 * * Funktioner * *
	 *******************************************/
	public String getName() {
		return name;
	}

	public String getPass() {
		return passwordstr;
	}

	public void updateDynamicInfo(String msg) {
		dynamicInfo.append(msg + "\n");
		dynamicInfo.setCaretPosition(dynamicInfo.getDocument().getLength());
	}
	public void setLabelCity() {
		playersInDungeon.setText(" Players In City: ");
	}

	public void setLabelDungeon() {
		playersInDungeon.setText(" Players In Dungeon: ");
	}

	public void setHealthStats(String stats) {
		healthStats.setText(" " + stats);
	}

	public void setCombatStats(String stats) {
		combatStats.setText(" " + stats);
	}

	public void setDungeonPlayers(String playerList) {
		dungeonPlayers.setText(playerList);
	}

	public void addDungeonPlayer(String player) {
		dungeonPlayers.append(player + "\n");
	}

	public void removeDungeonPlayer(String player) {
		String players = dungeonPlayers.getText();
		players = players.replace(player + "\n", "");
		dungeonPlayers.setText(players);
	}

	public void setRoomPlayers(String playerList) {
		roomPlayers.setText(playerList);
	}

	public void addRoomPlayer(String player) {
		roomPlayers.append(player + "\n");
	}

	public void removeRoomPlayer(String player) {
		String players = roomPlayers.getText();
		players = players.replace(player + "\n", "");
		roomPlayers.setText(players);
	}

	public void setNPCs(String npcsList) {
		npcs.setText(npcsList);
	}

	public void addNPC(String npc) {
		npcs.append(npc + "\n");
	}

	public void removeNPC(String npc) {
		String npctxt = npcs.getText();
		npctxt = npctxt.replace(npc + "\n", "");
		npcs.setText(npctxt);
	}

	public void setItems(String itemsList) {
		items.setText(itemsList);
	}

	public void addItem(String item) {
		items.append(item + "\n");
	}

	public void removeItem(String item) {
		String i = items.getText();
		item = item.replace("(", "\\(");
		item = item.replace(")", "\\)");
		i = i.replaceFirst(item + "\n", "");
		System.out.println("tar bort item " + item);

		items.setText(i);
	}

	public void setExits(String exitList) {
		exits.setText(exitList);
	}
}
