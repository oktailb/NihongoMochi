package org.nihongo.mochi.domain.services

interface AudioPlayer {
    /**
     * Joue un son court (effet sonore) à partir d'un chemin de ressource.
     */
    fun playSound(resourcePath: String)

    /**
     * Arrête tous les sons en cours.
     */
    fun stopAll()
    
    /**
     * Libère les ressources associées au lecteur.
     */
    fun release()
}
