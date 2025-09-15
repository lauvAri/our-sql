#!/bin/bash
# ttyd -W -t title="Our SQL" -t 'theme={"background": "#282c34", "foreground": "#abb2bf"}' java -jar ./our-sql-1.0-SNAPSHOT.jar

# ttyd -W -i ~/oursql/index.html java -jar ./our-sql-1.0-SNAPSHOT.jar

# ./ttyd.x86_64 -W -i ~/oursql/index.html java -jar ./our-sql-1.0-SNAPSHOT.jar

ttyd -W \
  -t title="Our SQL [CyberCore]" \
  -t fontFamily="'Fira Code', 'JetBrains Mono', monospace" \
  -t fontSize=16 \
  -t cursorStyle='block' \
  -t cursorBlink=true \
  -t 'theme={
      "background": "#1a102c",
      "foreground": "#00dfff",
      "cursor": "#ff00ff",
      "selectionBackground": "#3c2a6d",
      "black": "#6272a4",
      "red": "#ff5555",
      "green": "#50fa7b",
      "yellow": "#f1fa8c",
      "blue": "#bd93f9",
      "magenta": "#ff79c6",
      "cyan": "#8be9fd",
      "white": "#f8f8f2",
      "brightBlack": "#6272a4",
      "brightRed": "#ff6e6e",
      "brightGreen": "#69ff94",
      "brightYellow": "#ffffa5",
      "brightBlue": "#d6acff",
      "brightMagenta": "#ff92df",
      "brightCyan": "#a4ffff",
      "brightWhite": "#ffffff"
  }' \
  java -jar ./our-sql-1.0-SNAPSHOT.jar
