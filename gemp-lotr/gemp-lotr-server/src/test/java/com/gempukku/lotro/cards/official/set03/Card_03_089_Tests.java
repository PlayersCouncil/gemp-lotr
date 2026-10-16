package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gleaming", "3_89");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2, vitality 2; his text answers only a weapon played on him
					put("skirmisher", "1_257"); // Morgul Skirmisher: [sauron] Orc, cost 3, vitality 3; a maneuver ability used only on purpose
					put("troll", "6_106");      // Troll of Udûn: a [sauron] minion who is no Orc; a skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GleamingintheSnowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Gleaming in the Snow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert a [sauron] Orc and spot a [gondor] companion to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gleaming");

		assertEquals("Gleaming in the Snow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronOrcWhileAGondorCompanionIsSpottedToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gleaming = scn.GetShadowCard("gleaming");
		var chieftain = scn.GetShadowCard("chieftain");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gleaming);
		assertInHand(gleaming);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, skirmisher, troll, troop);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(skirmisher));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowPlayAvailable(gleaming));

		scn.ShadowPlayCard(gleaming);

		// Either [sauron] Orc; not the [sauron] Troll, not the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, skirmisher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(skirmisher);

		assertEquals(1, scn.GetWoundsOn(skirmisher));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(gleaming);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AGondorHobbitCompanionIsAGondorCompanionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gleaming = scn.GetShadowCard("gleaming");
		var chieftain = scn.GetShadowCard("chieftain");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(gleaming);
		assertInHand(gleaming);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.ShadowPlayAvailable(gleaming));

		scn.ShadowPlayCard(gleaming);

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWithoutAGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gleaming = scn.GetShadowCard("gleaming");
		var chieftain = scn.GetShadowCard("chieftain");
		var merchant = scn.GetFreepsCard("merchant");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Frodo is a companion of another culture; the Merchant is a [gondor] ally, no companion.
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(gleaming);
		assertInHand(gleaming);
		assertInZone(Zone.SUPPORT, merchant);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(gleaming));

		// A [gondor] companion to spot, and it can be played.
		scn.MoveCompanionsToTable(aragorn);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(gleaming));
	}

	@Test
	public void NotPlayableWhenTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gleaming = scn.GetShadowCard("gleaming");
		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gleaming);
		assertInHand(gleaming);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troll, troop);
		// Vitality 2 with 1 wound: the Orc Chieftain cannot exert.  The Troll and the [moria] Orc could.
		scn.AddWoundsToChar(chieftain, 1);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(gleaming));
	}
}
