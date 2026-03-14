package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_092_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher1", "103_92");
					put("watcher2", "103_92");
					put("stone", "9_47");         // Ithil Stone - [Sauron] artifact to spot
					put("aragorn", "1_89");       // Has Maneuver exert ability
					put("boromir", "1_97");
					put("gandalf", "1_364");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolWatcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cirith Ungol Watcher, Hideous Warden
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot a [sauron] card.
		* 	Each time a companion exerts, draw a card (limit once per site).
		* 	Response: If a companion is exerted by a Free Peoples card, discard another Cirith Ungol Watcher to add 2 threats and hinder that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watcher1");

		assertEquals("Cirith Ungol Watcher", card.getBlueprint().getTitle());
		assertEquals("Hideous Warden", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WatcherResponseAdds2ThreatsAndHindersCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(watcher1, watcher2);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCompanionsToTable("gandalf", "boromir");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		int threats = scn.GetThreats();
		assertFalse(scn.IsHindered(aragorn));

		// Aragorn exerts via his own (FP) ability - triggers the Response
		scn.FreepsUseCardAction(aragorn);

		// Response should be available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable("Watcher"));
		scn.ShadowAcceptOptionalTrigger();
		// One Watcher discarded as cost
		assertEquals(1, scn.GetShadowDiscardCount());
		// Errata: adds 2 threats (was 3 in original)
		assertEquals(threats + 2, scn.GetThreats());
		assertTrue(scn.IsHindered(aragorn));
	}
}
