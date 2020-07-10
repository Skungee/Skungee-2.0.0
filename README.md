# Skungee-2.0.0
Remake of the Skungee system (TCP), to be using Japson a QUIC/UDP protocol.

Alpha testing releases at https://github.com/Skungee/Skungee-2.0.0/packages/306647

Using the Skungee API:

Spigot:
```java
SpigotSkungee skungee = (SpigotSkungee) Bukkit.getPlugin("Skungee");
SkungeeAPI API = skungee.getAPI();

// Sending voids.
JsonObject json = new JsonObject();
json.addProperty("message", "Hello world!");

API.sendJson(json);

// Returning data.
JsonObject json = new JsonObject();
json.addProperty("message", "Hello world!");

String response = API.sendJson(json, object -> object.get("response").getAsString());

assertEquals(response, "This is a response!");
```

Proxy side:
```java
// Running our void
try {
	Skungee.getPlatform().setApiHandler(new Executor(Packets.API.getPacketId()) {

		@Override
		public void execute(InetAddress address, int port, JsonObject object) {
			if (!object.has("message"))
				return null;
			String message = object.get("message").getAsString();
			ProxyServer.getInstance().getPlayers().forEach(player -> player.sendMessage(message));
		}
		
	});
} catch (IllegalAccessException e) {
	e.printStackTrace();
}

// Returning data.
try {
	Skungee.getPlatform().setApiHandler(new Handler(Packets.API.getPacketId()) {

		@Override
		public JsonObject handle(InetAddress address, int port, JsonObject object) {
			if (!object.has("message"))
				return null;
			String message = object.get("message").getAsString();
			assertEquals(message, "Hello world!");

			JsonObject returning = new JsonObject();
			returning.addProperty("response", "This is a response!");
			return returning;
		}
		
	});
} catch (IllegalAccessException e) {
	e.printStackTrace();
}
```
