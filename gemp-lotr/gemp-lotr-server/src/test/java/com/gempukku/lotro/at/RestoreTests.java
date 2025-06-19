package com.gempukku.lotro.at;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.RestoreCardsInPlayEffect;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class RestoreTests {
	@Test
	public void RestoringACompanionInPlayFlipsItOver() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = new VirtualTableScenario(
				new HashMap<>() {{
					put("aragorn", "1_89");
				}}
		);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.HinderCard(aragorn);

		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
				RequiredTriggerAction action = new RequiredTriggerAction(frodo);
				action.appendEffect(new RestoreCardsInPlayEffect(scn.P1, null, aragorn));
				action.setText("Restore Aragorn");
				return Collections.singletonList(action);
			}
		});
		scn.StartGame();

		assertTrue(aragorn.isFlipped());

		scn.FreepsUseCardAction(frodo);

		assertFalse(aragorn.isFlipped());
	}

	@Test
	public void RestoringACompanionInPlayRemovesTheHinderedStatus() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = new VirtualTableScenario(
				new HashMap<>() {{
					put("aragorn", "1_89");
				}}
		);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.HinderCard(aragorn);

		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game)  {
				RequiredTriggerAction action = new RequiredTriggerAction(frodo);
				action.appendEffect(new RestoreCardsInPlayEffect(scn.P1, null, aragorn));
				action.setText("Restore Aragorn");
				return Collections.singletonList(action);
			}
		});
		scn.StartGame();

		assertTrue(aragorn.isFlipped());
		assertTrue(scn.IsHindered(aragorn));
		assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());

		scn.FreepsUseCardAction(frodo);

		assertFalse(aragorn.isFlipped());
		assertFalse(scn.IsHindered(aragorn));
	}

	@Test
	public void OnlyPlayerReconcileRestoresPlayerHinderedCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = new VirtualTableScenario(
				new HashMap<>() {{
					put("aragorn", "1_89");
					put("fodder", "1_90");
				}}
		);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.HinderCard(aragorn);

		//So that there's something to reconcile
		scn.ShadowDrawCard();
		scn.FreepsDrawCard();

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();

		//Shadow reconcile doesn't affect freeps
		assertTrue(scn.ShadowDecisionAvailable("reconcile"));
		assertTrue(scn.IsHindered(aragorn));
		scn.ShadowDeclineReconciliation();
		assertTrue(scn.IsHindered(aragorn));

		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("reconcile"));
		assertTrue(scn.IsHindered(aragorn));
		scn.FreepsDeclineReconciliation();
		assertFalse(scn.IsHindered(aragorn));
	}

	@Test
	public void ArtificialReconcileTriggersRestore() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = new VirtualTableScenario(
				new HashMap<>() {{
					put("aragorn", "1_89");
					put("fodder", "1_90");
					put("lembas", "4_75");
				}}
		);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var lembas = scn.GetFreepsCard("lembas");
		scn.MoveCompanionsToTable(aragorn);
		scn.HinderCard(aragorn);
		scn.AttachCardsTo(frodo, lembas);

		//So that there's something to reconcile
		scn.ShadowDrawCard();
		scn.FreepsDrawCard();

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsUseCardAction(lembas);

		assertTrue(scn.FreepsDecisionAvailable("reconcile"));
		assertTrue(scn.IsHindered(aragorn));
		scn.FreepsDeclineReconciliation(); //Actually declining the "discard a card from hand" part of reconcile
		assertFalse(scn.IsHindered(aragorn));

		assertTrue(scn.ShadowDecisionAvailable("Regroup"));
	}
}
