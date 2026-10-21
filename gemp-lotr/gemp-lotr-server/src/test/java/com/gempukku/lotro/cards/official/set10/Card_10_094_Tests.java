package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ravager", "10_94");

					put("chieftain", "1_266");      // Orc Chieftain: a [sauron] Orc; his trigger answers only a weapon played on him
					put("sentry", "10_84");         // Cirith Ungol Sentry: a [sauron] Uruk-hai, damage +1, no other text
					put("runner", "1_178");         // Goblin Runner: a [moria] minion; adds (2) when played, and is cheated in here
					put("fear", "1_278");           // Strength Born of Fear: [sauron] support-area condition; its skirmish ability is never used
					put("legacy", "1_282");         // The Weight of a Legacy: [sauron] condition on Aragorn, strength -1

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability only ever used on purpose
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire] companion; his abilities are used only on purpose
					put("saga", "1_114");           // The Saga of Elendil: [gondor] condition on a [gondor] companion; a skirmish ability only
					put("stairs", "1_24");          // Stairs of Khazad-dum: [dwarven] support-area condition
					put("sword", "1_112");          // Ranger's Sword: [gondor] possession on Aragorn, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcRavagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Orc Ravager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: To play, spot a [sauron] minion.<br><b>Shadow:</b> Remove 2 threats to discard a condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ravager");

		assertEquals("Orc Ravager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilASauronMinionIsSpottedAndThenGoesToTheShadowCharacters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager");
		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(ravager, chieftain);
		scn.MoveCardsToSupportArea(fear);
		assertInHand(ravager, chieftain);
		assertInZone(Zone.SUPPORT, fear);

		scn.StartGame();
		// A [moria] minion and a [sauron] card that is no minion.
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		// Enough for the Chieftain and the Ravager, each roaming at site 2.
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertInHand(ravager, chieftain);
		assertFalse(scn.ShadowPlayAvailable(ravager));
		assertTrue(scn.ShadowPlayAvailable(chieftain));

		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertTrue(scn.ShadowPlayAvailable(ravager));

		scn.ShadowPlayCard(ravager);
		assertInZone(Zone.SHADOW_CHARACTERS, ravager);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityRemovesTwoThreatsToDiscardAConditionOfEitherSideBorneOrInASupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager");
		var sentry = scn.GetShadowCard("sentry");
		var fear = scn.GetShadowCard("fear");
		var legacy = scn.GetShadowCard("legacy");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var saga = scn.GetFreepsCard("saga");
		var stairs = scn.GetFreepsCard("stairs");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AttachCardsTo(aragorn, saga, sword, legacy);
		scn.MoveCardsToSupportArea(stairs, fear);
		scn.MoveCardsToHand(ravager);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(legacy, aragorn);
		assertInZone(Zone.SUPPORT, stairs, fear);
		assertInHand(ravager);

		scn.StartGame();
		// A [sauron] Uruk-hai is the one [sauron] minion to spot for the play.
		scn.MoveMinionsToTable(sentry);
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		// Three companions, so three threats.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(ravager));
		scn.ShadowPlayCard(ravager);
		assertInZone(Zone.SHADOW_CHARACTERS, ravager);

		assertEquals(3, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(ravager));
		scn.ShadowUseCardAction(ravager);
		assertEquals(1, scn.GetThreats());

		// Conditions of both sides, borne and in a support area, the Shadow player's own among them; not a possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, stairs, legacy, fear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, aragorn, sentry));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saga);

		assertInDiscard(saga);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(legacy, aragorn);
		assertInZone(Zone.SUPPORT, stairs, fear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneThreatTheAbilityIsNotOfferedAndWithTwoItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stairs);
		assertInZone(Zone.SUPPORT, stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(ravager);
		assertInZone(Zone.SHADOW_CHARACTERS, ravager);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(ravager));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(ravager));
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayTheThreatsAreRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ravager = scn.GetShadowCard("ravager");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		assertAttachedTo(sword, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(ravager);
		assertInZone(Zone.SHADOW_CHARACTERS, ravager);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.FreepsPass();

		// The ability has a cost, so it is offered with nothing to discard (Q-0034).
		assertTrue(scn.ShadowActionAvailable(ravager));
		scn.ShadowUseCardAction(ravager);

		assertEquals(0, scn.GetThreats());
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
