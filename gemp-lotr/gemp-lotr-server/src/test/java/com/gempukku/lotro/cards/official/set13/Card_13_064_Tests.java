package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denethor", "13_64");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and no other text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally, a Man; a maneuver ability used only on purpose
					put("shield", "1_107");     // Great Shield: [gondor] possession (shield), "Bearer must be a Man"; archery total -1
					put("armor1", "1_92");      // Armor: [gondor] possession (armor), "Bearer must be a Man"; a wound cap in skirmishes
					put("armor2", "1_92");
					put("kingsfoil", "13_73");  // Kingsfoil: [gondor] possession on a [gondor] Man; Fellowship: discard it to heal a companion
					put("catapult", "8_32");    // Catapult: [gondor] support-area possession; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: [gondor] ARTIFACT hand weapon on a [gondor] Man, vitality +1
					put("chain", "4_278");      // Heavy Chain: [rohan] possession (armor), "Bearer must be a Man"; an overwhelm bar
					put("path", "8_42");        // A Path Appointed: [gondor] support-area condition
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession (hand weapon)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("scout", "1_191");      // Moria Scout: [moria] Orc of vitality 2; a play trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DenethorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Denethor, Last Ruling Steward
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: Denethor is resistance +1 for each [gondor] possession in your discard pile.<br><b>Maneuver:</b> Spot a [gondor] possession borne by a companion and remove that possession from the game to exert a minion twice.
		*/

		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");

		assertEquals("Denethor", denethor.getBlueprint().getTitle());
		assertEquals("Last Ruling Steward", denethor.getBlueprint().getSubtitle());
		assertTrue(denethor.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, denethor.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, denethor.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, denethor.getBlueprint().getCardType());
		assertEquals(Race.MAN, denethor.getBlueprint().getRace());
		assertEquals(3, denethor.getBlueprint().getTwilightCost());
		assertEquals(7, denethor.getBlueprint().getStrength());
		assertEquals(3, denethor.getBlueprint().getVitality());
		assertEquals(5, denethor.getBlueprint().getResistance());
	}

	@Test
	public void ResistanceIsPlusOneForEachGondorPossessionInHisOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var knight = scn.GetFreepsCard("knight");
		var kingsfoil = scn.GetFreepsCard("kingsfoil");
		var shield = scn.GetFreepsCard("shield");
		var catapult = scn.GetFreepsCard("catapult");
		var path = scn.GetFreepsCard("path");
		var narsil = scn.GetFreepsCard("narsil");
		var axe = scn.GetFreepsCard("axe");
		var shadowShield = scn.GetShadowCard("shield");
		scn.MoveCompanionsToTable(denethor, knight);
		scn.AttachCardsTo(denethor, kingsfoil);
		// A [gondor] possession in play is not in the discard pile.
		scn.AttachCardsTo(knight, shield);
		// A [gondor] condition, a [gondor] artifact and a [dwarven] possession in his discard pile, and a [gondor]
		// possession in the other player's.
		scn.MoveCardsToDiscard(path, narsil, axe, shadowShield);
		scn.AddWoundsToChar(knight, 1);

		scn.StartGame();
		assertAttachedTo(kingsfoil, denethor);
		assertAttachedTo(shield, knight);
		assertInDiscard(path, narsil, axe, shadowShield);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(5, scn.GetResistance(denethor));
		assertEquals(6, scn.GetResistance(knight));

		// Kingsfoil discards itself to heal the Knight, the only wounded companion.
		scn.FreepsUseCardAction(kingsfoil);
		assertInDiscard(kingsfoil);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(6, scn.GetResistance(denethor));
		assertEquals(6, scn.GetResistance(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second [gondor] possession, of another kind (a support-area card).
		scn.MoveCardsToDiscard(catapult);
		assertInDiscard(catapult);
		assertEquals(7, scn.GetResistance(denethor));
		assertEquals(6, scn.GetResistance(knight));
	}

	@Test
	public void ManeuverAbilityRemovesAGondorPossessionBorneByACompanionFromTheGameToExertAMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var shield = scn.GetFreepsCard("shield");
		var armor1 = scn.GetFreepsCard("armor1");
		var armor2 = scn.GetFreepsCard("armor2");
		var catapult = scn.GetFreepsCard("catapult");
		var narsil = scn.GetFreepsCard("narsil");
		var chain = scn.GetFreepsCard("chain");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(denethor, knight, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(denethor, shield);
		scn.AttachCardsTo(guard, armor1);
		// A [gondor] possession borne by an ally, one in the support area, a [gondor] artifact and a [rohan]
		// possession borne by a companion.
		scn.AttachCardsTo(merchant, armor2);
		scn.MoveCardsToSupportArea(catapult);
		scn.AttachCardsTo(knight, narsil, chain);

		scn.StartGame();
		assertAttachedTo(shield, denethor);
		assertAttachedTo(armor1, guard);
		assertAttachedTo(armor2, merchant);
		assertInZone(Zone.SUPPORT, catapult);
		assertAttachedTo(narsil, knight);
		assertAttachedTo(chain, knight);
		scn.MoveMinionsToTable(troop, savage, scout);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetResistance(denethor));
		assertTrue(scn.FreepsActionAvailable(denethor));
		scn.FreepsUseCardAction(denethor);
		assertTrue(scn.FreepsHasCardChoiceAvailable(shield, armor1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor2, catapult, narsil, chain));
		scn.FreepsChooseCard(armor1);
		// Removed from the game, not discarded: Denethor's resistance does not count it.
		assertInZone(Zone.REMOVED, armor1);
		assertEquals(5, scn.GetResistance(denethor));
		assertAttachedTo(shield, denethor);

		// The Scout, of vitality 2, cannot exert twice.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scout));
		// Not a companion, though Denethor and the Knight have the vitality to exert twice.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(denethor, knight));
		scn.FreepsChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(denethor));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithoutAGondorPossessionBorneByACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var armor2 = scn.GetFreepsCard("armor2");
		var catapult = scn.GetFreepsCard("catapult");
		var narsil = scn.GetFreepsCard("narsil");
		var chain = scn.GetFreepsCard("chain");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(denethor, knight);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, armor2);
		scn.MoveCardsToSupportArea(catapult);
		scn.AttachCardsTo(knight, narsil, chain);

		scn.StartGame();
		assertAttachedTo(armor2, merchant);
		assertInZone(Zone.SUPPORT, catapult);
		assertAttachedTo(narsil, knight);
		assertAttachedTo(chain, knight);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(denethor));
		assertAttachedTo(armor2, merchant);
		assertInZone(Zone.SUPPORT, catapult);
	}

	@Test
	public void WithNoMinionAbleToExertTwiceThePossessionIsStillRemovedAndTheMinionExertsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var shield = scn.GetFreepsCard("shield");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(denethor);
		scn.AttachCardsTo(denethor, shield);

		scn.StartGame();
		assertAttachedTo(shield, denethor);
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.FreepsActionAvailable(denethor));
		// The Shield is the only possession that can pay, and the Scout the only minion: neither is asked.
		scn.FreepsUseCardAction(denethor);
		assertInZone(Zone.REMOVED, shield);
		// Vitality 2: as much of "exert twice" as can be done.
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
