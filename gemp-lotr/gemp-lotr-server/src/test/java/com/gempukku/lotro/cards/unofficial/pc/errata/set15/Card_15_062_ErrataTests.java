package com.gempukku.lotro.cards.unofficial.pc.errata.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_062_ErrataTests
{
	// The Fellowship path. The tests that need a roaming minion are at site 2 (Ettenmoors), where a minion
	// roams if its site number is 3 or more.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade1", "65_62");
					put("blade2", "65_62");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4
					put("knight1", "5_35");     // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text
					put("knight2", "5_35");
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, strength 6; a maneuver ability never used here
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is its whole text

					// Site number above 2: roaming at site 2.
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, vitality 3, site 5; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, site 4; no text
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, vitality 2, site 5; Damage +1 is its whole text
					// Site number 2: not roaming at site 2.
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: vitality 3; nothing else below 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IthilienBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Ithilien Blade
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [gondor] Man.
		 * 	While you can spot a roaming minion, bearer is strength +1.
		 * 	<b>Maneuver:</b> Exert bearer twice to wound a roaming minion twice.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade1");

		assertEquals("Ithilien Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade1 = scn.GetFreepsCard("blade1");
		var blade2 = scn.GetFreepsCard("blade2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(blade1, blade2);

		scn.StartGame();

		scn.FreepsPlayCard(blade1);
		// A [gondor] companion and a [gondor] ally who are Men; not a [gondor] Hobbit, nor a Man of Rohan.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(blade1, aragorn);

		// Aragorn now holds a hand weapon, so the Merchant is the one bearer left for the second copy.
		scn.FreepsPlayCard(blade2);
		assertAttachedTo(blade2, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlusOneWhileARoamingMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade1 = scn.GetFreepsCard("blade1");
		var blade2 = scn.GetFreepsCard("blade2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		// A copy on each of two Men, and a third [gondor] Man who bears none.
		scn.MoveCompanionsToTable(aragorn, knight1, knight2);
		scn.AttachCardsTo(aragorn, blade1);
		scn.AttachCardsTo(knight1, blade2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(savage);

		scn.StartGame();
		// Plenty for the Uruk Savage: 2 to play + 2 roaming.
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// A minion at its own site number is not roaming.
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(10, scn.GetStrength(aragorn));     // 8 + 2
		assertEquals(8, scn.GetStrength(knight1));      // 6 + 2
		assertEquals(6, scn.GetStrength(knight2));

		scn.ShadowPlayCard(savage);
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		// Each bearer has the +1 of the copy he bears, and not the other copy's as well.
		assertEquals(11, scn.GetStrength(aragorn));     // 8 + 2 + 1
		assertEquals(9, scn.GetStrength(knight1));      // 6 + 2 + 1
		assertEquals(6, scn.GetStrength(knight2));

		// A second roaming minion, of another culture and race, adds nothing more.
		scn.MoveMinionsToTable(troop);
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(knight1));
		assertEquals(6, scn.GetStrength(knight2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsBearerTwiceToWoundARoamingMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade1 = scn.GetFreepsCard("blade1");
		var blade2 = scn.GetFreepsCard("blade2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight1 = scn.GetFreepsCard("knight1");
		scn.MoveCompanionsToTable(aragorn, knight1);
		scn.AttachCardsTo(aragorn, blade1);
		scn.AttachCardsTo(knight1, blade2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, nelya);
		scn.FreepsRefreshDecision();

		// Not a fellowship ability.
		assertFalse(scn.FreepsActionAvailable(blade1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(blade1));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.FreepsUseCardAction(blade1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nelya));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// The bearer of the copy that was used, and not the bearer of the other.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight1));
		// Vitality 3: two wounds leave the Savage in play.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheTwoWoundsKillARoamingMinionOfVitalityTwoAndTheStrengthBonusGoesWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade1 = scn.GetFreepsCard("blade1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, blade1);

		var warrior = scn.GetShadowCard("warrior");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(warrior, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(11, scn.GetStrength(aragorn));     // 8 + 2 + 1
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);

		// The Orthanc Warrior is the only roaming minion, and has vitality 2: he could not exert twice, and
		// one wound would leave him in play.
		scn.FreepsUseCardAction(blade1);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, warrior);
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		// No roaming minion is left to spot.
		assertEquals(10, scn.GetStrength(aragorn));     // 8 + 2
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void BearerWhoCanExertOnlyOnceCannotUseTheAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade1 = scn.GetFreepsCard("blade1");
		var blade2 = scn.GetFreepsCard("blade2");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(knight1, knight2);
		scn.AttachCardsTo(knight1, blade1);
		scn.AttachCardsTo(knight2, blade2);
		// Vitality 3 with 1 wound: the first Knight can exert once more and no further.
		scn.AddWoundsToChar(knight1, 1);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(knight2));
		assertFalse(scn.FreepsActionAvailable(blade1));
		assertTrue(scn.FreepsActionAvailable(blade2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoRoamingMinionTheAbilityIsStillOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade1 = scn.GetFreepsCard("blade1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, blade1);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(10, scn.GetStrength(aragorn));     // 8 + 2
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(nelya));
		// The ability has a cost, so it is on offer though its effect will find no minion.
		assertTrue(scn.FreepsActionAvailable(blade1));

		scn.FreepsUseCardAction(blade1);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
