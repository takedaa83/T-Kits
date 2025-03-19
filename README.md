# T-Kits 🎯
> Advanced crystal PvP kit management system

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
![Version](https://img.shields.io/badge/version-1.0-blue)
![Spigot](https://img.shields.io/badge/Spigot-1.21-orange)
[![Discord](https://img.shields.io/badge/Discord-takwda-7289DA)]()

## 📖 Overview

T-Kits is a powerful and intuitive kit management plugin specifically designed for crystal PvP servers. It offers seamless kit organization, sharing capabilities, and quality-of-life features that enhance the competitive PvP experience.

### ✨ Key Features

- **Comprehensive Kit Management**
  - 7 customizable kit slots per player
  - Integrated ender chest storage system
  - Advanced equipment organization
  - Premade kit templates

- **Crystal PvP Optimized**
  - Quick kit loading for intense combat
  - Efficient inventory arrangement
  - Auto-repair functionality
  - Smart consumable refill system

- **Kit Sharing System**
  - Temporary sharing codes
  - One-time use options
  - Configurable expiration times
  - Secure transfer system

## 🚀 Getting Started

### 📥 Installation

1. **Download** the latest release (link will be provided upon release).
2. **Place** the JAR file in your server's `plugins` folder.
3. **Restart** your server.
4. **Configure** `config.yml` to your preferences.

### ⚙️ Configuration

```yaml
# Example configuration
database:
  enabled: false
  host: "localhost"
  port: 3306
  database_name: "tkits"

# Kit sharing settings
one-time-codes: true
code-expiry-minutes: 5

# Feature toggles
regear-enabled: true
arrange-enabled: true
```

## 🎮 Commands

| Command           | Description                        | Permission    |
|-------------------|------------------------------------|---------------|
| `/kit` or `/k`    | Opens kit menu                     | `tkits.kit`   |
| `/k1` - `/k7`     | Quick-loads specific kit           | `tkits.kit`   |
| `/regear`         | Refills consumables                | `tkits.kit`   |
| `/arrange`        | Organizes inventory                | `tkits.kit`   |
| `/tkits reload`   | Reloads configuration              | `tkits.reload`|

## 🔒 Permissions

- `tkits.kit` - Basic kit usage (default: true)
- `tkits.edit` - Edit kit room and premade kits (default: op)
- `tkits.bypass` - Bypass world restrictions (default: op)
- `tkits.reload` - Reload plugin configuration (default: op)
- `tkits.*` - All permissions

## 💾 Storage Options

- **YAML Storage**
  - Default storage method
  - Automatic backups
  - No additional setup required

- **MySQL Support**
  - High-performance option
  - Connection pooling
  - Configurable settings

## 📋 Usage Example

### Loading a Kit
1. Open the kit menu using `/kit` or `/k`.
2. Select the desired kit slot (e.g., Kit 1) to load the kit.

### Sharing a Kit
1. Open the kit editor.
2. Click the share button to generate a unique code.
3. Share the generated code with other players.

### Arranging Inventory
1. Use the `/arrange` command to organize your inventory according to your last loaded kit.

### Refilling Consumables
1. Use the `/regear` command to refill consumables and repair your shield.

## 🛠️ Advanced Configuration

### MySQL Settings

```yaml
database:
  enabled: true
  host: "localhost"
  port: 3306
  database_name: "tkits"
  username: "root"
  password: "password"
  pool_size: 10
  idle_timeout: 300000
  connection_timeout: 10000
```

### Broadcast Messages

```yaml
loadkit-enabled: true
loadkit-prefix: "&7[&b&lT&3&lK&7] &f"
loadkit-suffix: " &7loaded a &bKit"
```

### Sound Effects

```yaml
sounds:
  kit-load: "ENTITY_EXPERIENCE_ORB_PICKUP"
  kit-save: "BLOCK_NOTE_BLOCK_PLING"
  menu-click: "UI_BUTTON_CLICK"
  error: "ENTITY_VILLAGER_NO"
  success: "ENTITY_PLAYER_LEVELUP"
```

## 🤝 Support

For support, contact me on Discord: `takwda`.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌟 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## ✨ Acknowledgments

- Crystal PvP community for inspiration
- Server owners for valuable feedback
- Contributors and testers

---

<p align="center">Made with ❤️ by <a href="https://github.com/takedaa83">takedaa83</a></p>
