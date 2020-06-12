//
//  AudioManager.swift
//
//  Created by Jerome Xiong on 2020/1/13.
//  Copyright © 2020 JeromeXiong. All rights reserved.
//

import UIKit
import MediaPlayer

open class AudioManager: NSObject {

    open var hasNext: Bool = false
    open var hasPrev: Bool = false

    open var playing: Bool = false

    open var onEvents: ((String)->Void)?

    public static let `default`: AudioManager = {
        return AudioManager()
    }()

    func setupRemoteCommandHandler(enabled: Bool) {
        let command = MPRemoteCommandCenter.shared()

        if enabled {
            command.pauseCommand.addTarget{ (event) -> MPRemoteCommandHandlerStatus in
                self.onEvents?("pause")
                return .success
            }
            command.playCommand.addTarget{ (event) -> MPRemoteCommandHandlerStatus in
                self.onEvents?("play")
                return .success
            }
            /*command.togglePlayPauseCommand.addTarget{ (event) -> MPRemoteCommandHandlerStatus in
                if self.player?.rate != 0 && self.player?.error == nil {
                    self.onEvents?("pause")
	            } else {
                    self.onEvents?("play")
	            }
                return .success
            }*/
            command.nextTrackCommand.addTarget{ (event) -> MPRemoteCommandHandlerStatus in
                self.onEvents?("next")
                return .success
            }
            command.previousTrackCommand.addTarget{ (event) -> MPRemoteCommandHandlerStatus in
                self.onEvents?("prev")
                return .success
            }
            command.nextTrackCommand.isEnabled = hasNext
            command.previousTrackCommand.isEnabled = hasPrev
        } else {
            command.pauseCommand.removeTarget(self)
            command.playCommand.removeTarget(self)
            command.togglePlayPauseCommand.removeTarget(self)
            if hasNext {
                command.nextTrackCommand.removeTarget(self)
            }
            if hasPrev {
                command.previousTrackCommand.removeTarget(self)
            }
        }
    }

    func setRemoteInfo(title: String, artist: String, duration: Int, currentPosition: Int) {
        let center = MPNowPlayingInfoCenter.default()
        var infos = [String: Any]()
        
        infos[MPMediaItemPropertyTitle] = title
        infos[MPMediaItemPropertyArtist] = artist
        if duration > 0 {
            infos[MPMediaItemPropertyPlaybackDuration] = Double(duration / 1000)
            infos[MPNowPlayingInfoPropertyElapsedPlaybackTime] = Double(currentPosition / 1000)
        }
        
        center.nowPlayingInfo = infos
    }

    deinit {
        setupRemoteCommandHandler(enabled: false)
    }
}