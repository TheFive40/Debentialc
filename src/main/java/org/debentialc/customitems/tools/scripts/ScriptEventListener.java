package org.debentialc.customitems.tools.scripts;

import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.debentialc.Main;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

import java.lang.reflect.Method;

/**
 * Listener dinámico para eventos de Bukkit desde scripts JavaScript
 * Permite registrar listeners para cualquier evento de Bukkit en tiempo de ejecución
 *
 * FUNCIONAMIENTO:
 * 1. El script registra un listener: api.on("PlayerInteractEvent", function(event) {...})
 * 2. Se crea un ScriptEventListener que intercepta ese evento
 * 3. Cuando el evento ocurre, se ejecuta la función JavaScript
 */
public class ScriptEventListener implements Listener, EventExecutor {
    private final String scriptId;
    private final String eventName;
    private final Function callback;
    private final EventPriority priority;
    private final Class<? extends Event> eventClass;
    private boolean registered = false;

    public ScriptEventListener(String scriptId, String eventName, Function callback, EventPriority priority)
            throws ClassNotFoundException {
        this.scriptId = scriptId;
        this.eventName = eventName;
        this.callback = callback;
        this.priority = priority;

        // Intentar encontrar la clase del evento
        this.eventClass = findEventClass(eventName);

        if (this.eventClass == null) {
            throw new ClassNotFoundException("No se encontró el evento: " + eventName);
        }

        // Registrar el evento
        registerEvent();
    }

    /**
     * Busca la clase del evento en los paquetes comunes de Bukkit
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Event> findEventClass(String eventName) {
        // Paquetes donde buscar eventos
        String[] packages = {
                "org.bukkit.event.player",
                "org.bukkit.event.entity",
                "org.bukkit.event.block",
                "org.bukkit.event.inventory",
                "org.bukkit.event.world",
                "org.bukkit.event.server",
                "org.bukkit.event.vehicle",
                "org.bukkit.event.weather",
                "org.bukkit.event.hanging",
                "org.bukkit.event.painting",
                "org.bukkit.event.enchantment"
        };

        // Si ya incluye el paquete completo
        if (eventName.contains(".")) {
            try {
                return (Class<? extends Event>) Class.forName(eventName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        // Buscar en cada paquete
        for (String pkg : packages) {
            try {
                String fullName = pkg + "." + eventName;
                Class<?> clazz = Class.forName(fullName);

                if (Event.class.isAssignableFrom(clazz)) {
                    return (Class<? extends Event>) clazz;
                }
            } catch (ClassNotFoundException ignored) {
                // Continuar buscando
            }
        }

        return null;
    }

    /**
     * Registra el evento en Bukkit
     */
    private void registerEvent() {
        try {
            // Registrar usando EventExecutor para mayor flexibilidad
            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    this,
                    priority,
                    this,
                    Main.instance,
                    false
            );

            registered = true;
            Bukkit.getLogger().info("[ScriptEventListener] Registrado: " + eventName +
                    " (prioridad: " + priority + ") para script: " + scriptId);
        } catch (Exception e) {
            Bukkit.getLogger().severe("[ScriptEventListener] Error registrando evento " + eventName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método llamado por Bukkit cuando ocurre el evento
     */
    @Override
    public void execute(Listener listener, Event event) throws EventException {
        if (!event.getClass().equals(eventClass)) {
            return;
        }

        // Ejecutar el callback de JavaScript
        executeCallback(event);
    }

    /**
     * Ejecuta la función JavaScript del callback
     */
    private void executeCallback(Event event) {
        Context ctx = Context.enter();
        try {
            // Crear scope para la ejecución
            org.mozilla.javascript.Scriptable scope = callback.getParentScope();

            // Ejecutar la función pasando el evento como argumento
            callback.call(ctx, scope, null, new Object[]{event});

        } catch (Exception e) {
            Bukkit.getLogger().warning("[ScriptEventListener] Error ejecutando callback para " +
                    eventName + " en script " + scriptId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            Context.exit();
        }
    }

    /**
     * Desregistra el listener
     */
    public void unregister() {
        if (registered) {
            try {
                // Desregistrar manualmente
                HandlerList handlerList = getHandlerList(eventClass);
                if (handlerList != null) {
                    handlerList.unregister(this);
                }

                registered = false;
                Bukkit.getLogger().info("[ScriptEventListener] Desregistrado: " + eventName + " para script: " + scriptId);
            } catch (Exception e) {
                Bukkit.getLogger().warning("[ScriptEventListener] Error desregistrando " + eventName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Obtiene el HandlerList de un evento mediante reflexión
     */
    private HandlerList getHandlerList(Class<? extends Event> eventClass) {
        try {
            Method method = eventClass.getMethod("getHandlerList");
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[ScriptEventListener] No se pudo obtener HandlerList para: " + eventClass.getName());
            return null;
        }
    }

    // Getters
    public String getScriptId() {
        return scriptId;
    }

    public String getEventName() {
        return eventName;
    }

    public boolean isRegistered() {
        return registered;
    }
}