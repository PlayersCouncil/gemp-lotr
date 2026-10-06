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
					put("armory", "1_173"); //Goblin Armory: a Shadow condition in the support area
				}}
		);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(armory);
		scn.HinderCard(aragorn, armory);

		//So that there's something to reconcile
		scn.ShadowDrawCard();
		scn.FreepsDrawCard();

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.IsHindered(armory));
		scn.PassRegroupActions();

		//Shadow reconcile restores the Shadow player's own card before the discard is asked, and doesn't affect freeps
		assertTrue(scn.ShadowDecisionAvailable("reconcile"));
		assertFalse(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(aragorn));
		scn.ShadowDeclineReconciliation();
		assertTrue(scn.IsHindered(aragorn));

		scn.FreepsChooseToStay();
		//CR 5.0 "reconcile": the restoring comes before any card is discarded from hand (Ketura on Q-0059)
		assertTrue(scn.FreepsDecisionAvailable("reconcile"));
		assertFalse(scn.IsHindered(aragorn));
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
		assertTrue(scn.IsHindered(aragorn));
		scn.FreepsUseCardAction(lembas);

		//Restored as the reconcile begins, with the discard still to be asked (CR 5.0 "reconcile"; Ketura on Q-0059)
		assertTrue(scn.FreepsDecisionAvailable("reconcile"));
		assertFalse(scn.IsHindered(aragorn));
		scn.FreepsDeclineReconciliation(); //Actually declining the "discard a card from hand" part of reconcile
		assertFalse(scn.IsHindered(aragorn));

		assertTrue(scn.ShadowDecisionAvailable("Regroup"));
	}
}
