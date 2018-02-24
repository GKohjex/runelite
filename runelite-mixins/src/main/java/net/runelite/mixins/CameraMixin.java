/*
 * Copyright (c) 2018 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.mixins;

import net.runelite.api.mixins.FieldHook;
import net.runelite.api.mixins.Inject;
import net.runelite.api.mixins.Mixin;
import net.runelite.api.mixins.Shadow;
import net.runelite.rs.api.RSClient;

@Mixin(RSClient.class)
public abstract class CameraMixin implements RSClient
{
	private static final int STANDARD_PITCH_MAX = 383;
	private static final int NEW_PITCH_MAX = 512;

	@Shadow("clientInstance")
	static RSClient client;

	@Inject
	static boolean pitchRelaxEnabled = false;

	@Shadow("visibilityMaps")
	static boolean[][][][] visibilityMaps;

	@Inject
	static int lastPitch = 128;

	static
	{
		// The first index is pitch. In the default client it is 9, here it is 13 because we increase the pitch limit
		visibilityMaps = new boolean[13][35][53][53];
		for (boolean[][][] z : visibilityMaps)
		{
			for (boolean[][] y : z)
			{
				for (boolean[] x : y)
				{
					for (int i = 0; i < x.length; i++)
					{
						x[i] = true;
					}
				}
			}
		}
	}

	@FieldHook("cameraPitch")
	@Inject
	static void onCameraPitchChanged(int idx)
	{
		int newPitch = client.getCameraPitch();
		int pitch = newPitch;
		if (pitchRelaxEnabled)
		{
			// This works because the vanilla camera movement code only moves %2
			if (lastPitch > STANDARD_PITCH_MAX && newPitch == STANDARD_PITCH_MAX)
			{
				pitch = lastPitch;
				if (pitch > NEW_PITCH_MAX)
				{
					pitch = NEW_PITCH_MAX;
				}
				client.setCameraPitch(pitch);
			}
		}
		lastPitch = pitch;
	}

	@Inject
	public void setCameraPitchRelaxerEnabled(boolean enabled)
	{
		if (pitchRelaxEnabled == enabled)
		{
			return;
		}
		pitchRelaxEnabled = enabled;
		if (!enabled)
		{
			int pitch = client.getCameraPitch();
			if (pitch > STANDARD_PITCH_MAX)
			{
				client.setCameraPitch(STANDARD_PITCH_MAX);
			}
		}
	}
}