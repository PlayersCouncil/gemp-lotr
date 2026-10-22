package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("larder", "8_23");
					put("larder2", "8_23");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, cost 2, strength 5, site 3; a regroup ability used only on purpose
					put("shelob", "10_23");     // Shelob, Her Ladyship: [gollum] Spider, strength 8, enduring, fierce; her text answers only her own play
					put("smeagol", "6_45");     // Sméagol, Poor Creature: a [gollum] companion; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its (2) answers only its own play
					put("assassin", "1_262");   // Orc Assassin: [sauron] Orc, tracker; nothing that answers a discard
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, no Orc; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LarderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Larder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: To play, spot a [gollum] minion.
		 * 	Regroup: Discard an Orc from hand to add a [gollum] token here.
		 * 	Skirmish: Remove a [gollum] token here to make Shelob strength +3 (limit +6).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("larder");

		assertEquals("Larder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAGollumMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// A [gollum] companion is no [gollum] minion; the Goblin Runner is a minion of another culture.
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var larder = scn.GetShadowCard("larder");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(larder, gollum);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(larder));

		scn.ShadowPlayCard(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.ShadowPlayAvailable(larder));

		scn.ShadowPlayCard(larder);
		assertInZone(Zone.SUPPORT, larder);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShelobIsAGollumMinionThatLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var larder = scn.GetShadowCard("larder");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToHand(larder);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertTrue(scn.ShadowPlayAvailable(larder));
		scn.ShadowPlayCard(larder);
		assertInZone(Zone.SUPPORT, larder);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsAnOrcFromHandToAddAGollumTokenToThisLarder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var larder = scn.GetShadowCard("larder");
		var larder2 = scn.GetShadowCard("larder2");
		var runner = scn.GetShadowCard("runner");
		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(larder, larder2);
		scn.MoveCardsToHand(runner, assassin, savage);
		assertInZone(Zone.HAND, runner, assassin, savage);

		scn.StartGame();
		// No minion in play: the turn goes from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInZone(Zone.HAND, runner, assassin, savage);
		assertEquals(0, scn.GetCultureTokensOn(larder));
		assertEquals(0, scn.GetCultureTokensOn(larder2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(larder));

		scn.ShadowUseCardAction(larder);
		assertEquals(1, scn.ShadowGetChoiceMin());
		// Orcs of two cultures; an Uruk-hai is no Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, assassin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		scn.ShadowChooseCard(assassin);

		assertInZone(Zone.DISCARD, assassin);
		assertInZone(Zone.HAND, runner, savage);
		assertEquals(1, scn.GetCultureTokensOn(larder));
		// The token goes on the Larder whose ability was used.
		assertEquals(0, scn.GetCultureTokensOn(larder2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoOrcInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var larder = scn.GetShadowCard("larder");
		var savage = scn.GetShadowCard("savage");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(larder);
		scn.MoveCardsToHand(savage, gollum);
		assertInZone(Zone.HAND, savage, gollum);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInZone(Zone.HAND, savage, gollum);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(larder));
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var larder = scn.GetShadowCard("larder");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(larder);
		scn.MoveCardsToHand(runner);
		assertInZone(Zone.HAND, runner);

		scn.StartGame();
		// A minion in play, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.HAND, runner);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(larder));

		scn.ShadowPass();
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(larder));
	}

	@Test
	public void SkirmishAbilityRemovesATokenHereToMakeShelobStrongerUpToSixForEachLarderEachPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var larder = scn.GetShadowCard("larder");
		var larder2 = scn.GetShadowCard("larder2");
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(larder, larder2);
		scn.AddTokensToCard(larder, 4);
		scn.AddTokensToCard(larder2, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		assertEquals(4, scn.GetCultureTokensOn(larder));
		assertEquals(1, scn.GetCultureTokensOn(larder2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, shelob}, new PhysicalCardImpl[]{frodo, gollum});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(shelob));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(larder));

		// Shelob is the one card the text names; Gollum, another [gollum] minion, is left as he was.
		scn.ShadowUseCardAction(larder);
		assertEquals(3, scn.GetCultureTokensOn(larder));
		assertEquals(1, scn.GetCultureTokensOn(larder2));
		assertEquals(11, scn.GetStrength(shelob));
		assertEquals(5, scn.GetStrength(gollum));

		scn.FreepsPass();
		scn.ShadowUseCardAction(larder);
		assertEquals(2, scn.GetCultureTokensOn(larder));
		assertEquals(14, scn.GetStrength(shelob));

		// At its limit of +6 this phase: still offered, the token is paid, and Shelob gains nothing.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(larder));
		scn.ShadowUseCardAction(larder);
		assertEquals(1, scn.GetCultureTokensOn(larder));
		assertEquals(14, scn.GetStrength(shelob));

		// The limit is each copy's own (CR 4.2 "limit"): the other Larder still gives +3.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(larder2));
		scn.ShadowUseCardAction(larder2);
		assertEquals(0, scn.GetCultureTokensOn(larder2));
		assertEquals(17, scn.GetStrength(shelob));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// The next skirmish is a phase of its own: the bonus is gone, and the limit is new.
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.ShadowActionAvailable(larder));
		scn.ShadowUseCardAction(larder);
		assertEquals(0, scn.GetCultureTokensOn(larder));
		assertEquals(11, scn.GetStrength(shelob));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoTokenOnThisLarder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var larder = scn.GetShadowCard("larder");
		var larder2 = scn.GetShadowCard("larder2");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(larder, larder2);
		scn.AddTokensToCard(larder2, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		assertEquals(0, scn.GetCultureTokensOn(larder));
		assertEquals(1, scn.GetCultureTokensOn(larder2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(larder));
		assertTrue(scn.ShadowActionAvailable(larder2));
	}

	@Test
	public void SkirmishAbilityWithNoShelobInPlayRemovesTheTokenForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var larder = scn.GetShadowCard("larder");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(larder);
		scn.AddTokensToCard(larder, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		assertEquals(1, scn.GetCultureTokensOn(larder));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(larder));
		scn.ShadowUseCardAction(larder);

		assertEquals(0, scn.GetCultureTokensOn(larder));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
