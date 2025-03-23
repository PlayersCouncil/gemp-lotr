package com.gempukku.lotro.logic.effects.choose;

import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

public abstract class ChooseOpponentEffect extends UnrespondableEffect {
	private final String _playerId;

	public ChooseOpponentEffect(String playerId) {
		_playerId = playerId;
	}

	@Override
	public void doPlayEffect(LotroGame game) {
		String[] opponents = GameUtils.getOpponents(game, _playerId);
		if (opponents.length == 1)
			opponentChosen(opponents[0]);
		else
			game.getUserFeedback().sendAwaitingDecision(_playerId,
					new MultipleChoiceAwaitingDecision(1, "Choose an opponent", opponents) {
						@Override
						protected void validDecisionMade(int index, String result) {
							opponentChosen(result);
						}
					});
	}

	protected abstract void opponentChosen(String opponentId);
}
