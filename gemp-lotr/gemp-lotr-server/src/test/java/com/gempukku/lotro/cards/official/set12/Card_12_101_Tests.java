package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("retribution", "12_101");

					// Ordnance Grunt: [orc] Troll, strength 12, vitality 3, damage +1, fierce; its other texts are on its play
					put("grunt", "13_117");
					put("entrapping", "11_120"); // Entrapping Orc: [orc] Orc, strength 9; an assignment ability only
					// Unyielding Goblin: [orc] Orc, strength 5, vitality 2; its skirmish ability wants an underground site
					put("unyielding", "11_142");
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc, strength 5; its trigger is on being played

					put("spear", "12_96");       // Orc Spear: [orc] hand weapon, +2, cost 1, on an [orc] minion; a play trigger only
					put("hammer", "11_130");     // Orc Hammer: [orc] hand weapon, +1, cost 2, on an [orc] Orc; a play trigger only
					// Goblin Scimitar (The Hobbit): a [moria] hand weapon, +2, on ANY Orc: the one weapon of another culture an
					// [orc] Orc can bear; its texts are on its play
					put("scimitar", "31_31");
					// Bladed Gauntlets: [orc] armor, no weapon, on an [orc] minion; makes bearer a lurker; a skirmish ability
					put("gauntlets", "11_109");
					put("axe", "1_190");         // Moria Axe: [moria] hand weapon, +2, damage +1, on a [moria] Orc
					put("orkishax", "18_84");    // Orkish Ax: [orc] hand weapon, cost 1, on an [orc] Orc
					put("mordorscimitar", "11_128"); // Mordor Scimitar: [orc] hand weapon, cost 2, on an [orc] minion

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");        // Gimli, Son of Glóin: strength 6, vitality 3, damage +1; a skirmish ability
					put("boromir", "1_97");      // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RetributionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Retribution
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time an [orc] minion bearing a weapon wins a skirmish, add a burden.<br><b>Shadow:</b> Discard this condition to play an [orc] weapon from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("retribution");

		assertEquals("Retribution", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachWinOfAnOrcMinionBearingAWeaponAddsABurdenTheFierceTrollTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var retribution = scn.GetShadowCard("retribution");
		var grunt = scn.GetShadowCard("grunt");
		var entrapping = scn.GetShadowCard("entrapping");
		var spear = scn.GetShadowCard("spear");
		var scimitar = scn.GetShadowCard("scimitar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToSupportArea(retribution);
		scn.MoveMinionsToTable(grunt, entrapping);
		// An [orc] Troll bearing an [orc] weapon, and an [orc] Orc bearing a [moria] one.
		scn.AttachCardsTo(grunt, spear);
		scn.AttachCardsTo(entrapping, scimitar);
		assertInZone(Zone.SUPPORT, retribution);
		assertAttachedTo(spear, grunt);
		assertAttachedTo(scimitar, entrapping);
		assertEquals(0, scn.FreepsGetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, grunt}, new PhysicalCardImpl[]{gimli, entrapping});

		// 9 + 2 against Gimli's 6.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(11, scn.GetStrength(entrapping));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.FreepsGetBurdens());

		// 12 + 2 against Aragorn's 8, damage +1.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(14, scn.GetStrength(grunt));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.FreepsGetBurdens());

		// The fierce round: the same Troll wins again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, grunt);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DEAD, aragorn);
		assertEquals(3, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoBurdenForAnOrcMinionBearingAnItemThatIsNoWeaponAMoriaOrcWithAWeaponOrALossOfAnArmedOrcMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var retribution = scn.GetShadowCard("retribution");
		var entrapping = scn.GetShadowCard("entrapping");
		var runner = scn.GetShadowCard("runner");
		var unyielding = scn.GetShadowCard("unyielding");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var axe = scn.GetShadowCard("axe");
		var spear = scn.GetShadowCard("spear");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		scn.StartGame();
		scn.MoveCardsToSupportArea(retribution);
		scn.MoveMinionsToTable(entrapping, runner, unyielding);
		scn.AttachCardsTo(entrapping, gauntlets);
		scn.AttachCardsTo(runner, axe);
		scn.AttachCardsTo(unyielding, spear);
		assertInZone(Zone.SUPPORT, retribution);
		assertAttachedTo(gauntlets, entrapping);
		assertAttachedTo(axe, runner);
		assertAttachedTo(spear, unyielding);
		assertEquals(0, scn.FreepsGetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, entrapping}, new PhysicalCardImpl[]{gimli, runner},
				new PhysicalCardImpl[]{aragorn, unyielding});

		// A [moria] Orc bearing a weapon wins: 5 + 2 against Gimli's 6.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(7, scn.GetStrength(runner));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.FreepsGetBurdens());

		// An [orc] Orc bearing a weapon loses, and lives: 5 + 2 against Aragorn's 8.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(7, scn.GetStrength(unyielding));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(unyielding));
		assertInZone(Zone.SHADOW_CHARACTERS, unyielding);
		assertEquals(0, scn.FreepsGetBurdens());

		// An [orc] Orc bearing armor, no weapon, wins (a lurker by the Gauntlets, so last): 9 against Boromir's 7.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(9, scn.GetStrength(entrapping));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowAbilityDiscardsItToPlayAnOrcWeaponFromItsOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var retribution = scn.GetShadowCard("retribution");
		var entrapping = scn.GetShadowCard("entrapping");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");
		var hammer = scn.GetShadowCard("hammer");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var axe = scn.GetShadowCard("axe");
		var orkishax = scn.GetShadowCard("orkishax");
		// An [orc] weapon in the other player's discard pile, of a title in none of the Shadow player's piles
		var mordorscimitar = scn.GetFreepsCard("mordorscimitar");
		scn.MoveCardsToDiscard(spear, hammer, gauntlets, axe, mordorscimitar);
		scn.MoveCardsToHand(orkishax);

		scn.StartGame();
		scn.MoveCardsToSupportArea(retribution);
		// The Goblin Runner can bear the Moria Axe, and either Orc the Gauntlets.
		scn.MoveMinionsToTable(entrapping, runner);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, retribution);
		assertInZone(Zone.DISCARD, spear, hammer, gauntlets, axe, mordorscimitar);
		assertInHand(orkishax);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(retribution));
		scn.ShadowUseCardAction(retribution);

		// Two [orc] weapons from his own discard pile; not the [orc] armor, the [moria] weapon, the [orc] weapon in his
		// hand or the one in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, hammer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gauntlets, axe, orkishax, mordorscimitar));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hammer);

		// The Hammer goes on the one [orc] Orc unasked; its own "you may exert a companion" is declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hammer));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, retribution);
		assertAttachedTo(hammer, entrapping);
		assertInZone(Zone.DISCARD, spear, gauntlets, axe, mordorscimitar);
		assertInHand(orkishax);
		assertEquals(0, scn.GetAttachedCards(runner).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoOrcWeaponInTheDiscardPileOrTheTwilightOneShortOfItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var retribution = scn.GetShadowCard("retribution");
		var entrapping = scn.GetShadowCard("entrapping");
		var runner = scn.GetShadowCard("runner");
		var hammer = scn.GetShadowCard("hammer");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToDiscard(gauntlets, axe);

		scn.StartGame();
		scn.MoveCardsToSupportArea(retribution);
		scn.MoveMinionsToTable(entrapping, runner);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, retribution);
		assertInZone(Zone.DISCARD, gauntlets, axe);
		scn.FreepsPass();

		// Only an [orc] armor and a [moria] weapon there: playing one is the price, and none can be paid.
		assertFalse(scn.ShadowActionAvailable(retribution));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Orc Hammer, of cost 2, with 1 in the pool.
		scn.MoveCardsToDiscard(hammer);
		assertInZone(Zone.DISCARD, hammer);
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(retribution));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// With 2 it is offered.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(retribution));
		assertInZone(Zone.SUPPORT, retribution);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
