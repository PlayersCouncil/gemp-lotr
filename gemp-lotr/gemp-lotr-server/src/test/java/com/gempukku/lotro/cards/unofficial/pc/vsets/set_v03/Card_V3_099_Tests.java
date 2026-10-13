package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_099_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("prisoners1", "103_99");
					put("prisoners2", "103_99");
					put("swordsman", "3_98");     // Orc Swordsman: [sauron] Orc, vitality 3; stronger while he bears a weapon
					put("cirith", "10_81");       // Cirith Ungol Guard: [sauron] Uruk-hai, vitality 2; damage +1 and nothing else
					put("slayer", "3_93");        // Morgul Slayer: [sauron] Orc, vitality 2; a regroup ability
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3; no text
					put("gollum", "6_40");        // Gollum, Old Villain: [gollum] minion, vitality 4; a skirmish ability
					put("guard1", "5_83");        // Household Guard: [rohan] Man companion, vitality 3; valiant and nothing else
					put("guard2", "5_83");
					put("aldor", "4_262");        // Aldor, Soldier of Edoras: [rohan] Man ally, vitality 2; an archery ability that needs a villager
					put("knight1", "5_35");       // Gondorian Knight: [gondor] Man companion, vitality 3; knight and nothing else
					put("knight2", "5_35");
					put("merchant", "7_97");      // Gondorian Merchant: [gondor] Man ally; a maneuver ability that needs initiative
					put("merry", "1_303");        // Merry, From O'er the Brandywine: [shire] Hobbit companion; stronger while he bears a weapon
					put("sam", "1_311");          // Sam, Son of Hamfast: [shire] Hobbit companion, vitality 4; a fellowship ability
					put("bilbo", "1_284");        // Bilbo, Retired Adventurer: [shire] Hobbit ally, vitality 3; a fellowship ability
					put("smeagol", "5_29");       // Sméagol, Slinker: [gollum] companion; a skirmish ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ReleasethePrisonersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Release the Prisoners!
		 * Unique: false
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Maneuver: Exert a [sauron] minion and remove a companion in the dead pile from the game to exert
		 * 		every character sharing a culture with that companion.  Hinder this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("prisoners1");

		assertEquals("Release the Prisoners!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForTwoToTheSupportAreaAndOffersItsAbilityInTheManeuverPhaseOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prisoners = scn.GetShadowCard("prisoners1");
		var swordsman = scn.GetShadowCard("swordsman");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCardsToHand(prisoners);
		scn.MoveCardsToDeadPile(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(prisoners));

		scn.ShadowPlayCard(prisoners);

		assertInZone(Zone.SUPPORT, prisoners);
		assertFalse(scn.IsHindered(prisoners));

		// A [sauron] minion able to exert and a companion in the dead pile, in each of three phases.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(prisoners));

		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(prisoners));

		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(prisoners));
	}

	@Test
	public void ExertsASauronMinionAndRemovesADeadCompanionToExertEveryCharacterOfThatCultureThenHindersItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prisoners1 = scn.GetShadowCard("prisoners1");
		var prisoners2 = scn.GetShadowCard("prisoners2");
		var swordsman = scn.GetShadowCard("swordsman");
		var cirith = scn.GetShadowCard("cirith");
		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");
		var shadowSam = scn.GetShadowCard("sam");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var aldor = scn.GetFreepsCard("aldor");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var merchant = scn.GetFreepsCard("merchant");
		var merry = scn.GetFreepsCard("merry");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(sam, guard2, knight1);
		scn.MoveAlliesToTable(aldor, bilbo);
		// In the Free Peoples player's dead pile: a [rohan] companion, a [shire] companion and an ally.
		scn.MoveCardsToDeadPile(guard1, merry, merchant);
		// A companion in his discard pile, and one in the Shadow player's own dead pile.
		scn.MoveCardsToDiscard(knight2);
		scn.MoveCardsToDeadPile(shadowSam);
		scn.MoveCardsToSupportArea(prisoners1, prisoners2);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman, cirith, slayer, savage);
		// Vitality 2 with 1 wound: the Slayer is exhausted.
		scn.AddWoundsToChar(slayer, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(prisoners1));
		assertTrue(scn.ShadowActionAvailable(prisoners2));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(cirith));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(aldor));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.ShadowUseCardAction(prisoners1);

		// A [sauron] Orc and a [sauron] Uruk-hai; not an [isengard] Uruk-hai, nor a [sauron] Orc who is exhausted.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swordsman, cirith));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, slayer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(swordsman);

		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(cirith));
		// Only the companions in the Free Peoples player's dead pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard1, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(merchant, knight2, knight1, shadowSam, sam));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard1);

		assertInZone(Zone.REMOVED, guard1);
		assertInZone(Zone.DEAD, merry, merchant, shadowSam);
		assertInZone(Zone.DISCARD, knight2);
		// Every [rohan] character, a companion and an ally, has exerted once.
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(1, scn.GetWoundsOn(aldor));
		// A [gondor] Man, and the [shire] characters, whose culture is that of the companion left in the dead pile.
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		// Of the minions only the one that paid has exerted, and that once.
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(cirith));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(slayer));
		// The copy used is hindered where it lies, and the other copy is not.
		assertInZone(Zone.SUPPORT, prisoners1, prisoners2);
		assertTrue(scn.IsHindered(prisoners1));
		assertFalse(scn.IsHindered(prisoners2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(prisoners1));
		assertTrue(scn.ShadowActionAvailable(prisoners2));

		scn.ShadowUseCardAction(prisoners2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(swordsman, cirith));
		scn.ShadowChooseCard(cirith);

		// Merry is the one companion left in the dead pile and is taken without a choice.
		assertInZone(Zone.REMOVED, merry, guard1);
		assertInZone(Zone.DEAD, merchant, shadowSam);
		// Every [shire] character: the Ring-bearer, another companion and an ally.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertEquals(1, scn.GetWoundsOn(cirith));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.IsHindered(prisoners1));
		assertTrue(scn.IsHindered(prisoners2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AMinionOfTheRemovedCompanionsCultureExertsWithTheRest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prisoners = scn.GetShadowCard("prisoners1");
		var swordsman = scn.GetShadowCard("swordsman");
		var cirith = scn.GetShadowCard("cirith");
		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDeadPile(smeagol);
		scn.MoveCardsToSupportArea(prisoners);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman, cirith, gollum);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(cirith));
		assertTrue(scn.ShadowActionAvailable(prisoners));

		scn.ShadowUseCardAction(prisoners);
		// Gollum is a minion, but not a [sauron] one, and cannot pay.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swordsman, cirith));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gollum));
		scn.ShadowChooseCard(swordsman);

		// Sméagol was a [gollum] companion, and Gollum is a [gollum] character.
		assertInZone(Zone.REMOVED, smeagol);
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(cirith));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.IsHindered(prisoners));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoCharacterOfThatCultureInPlayTheCostsArePaidAndItIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prisoners = scn.GetShadowCard("prisoners1");
		var swordsman = scn.GetShadowCard("swordsman");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight1");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToDeadPile(guard);
		scn.MoveCardsToSupportArea(prisoners);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertTrue(scn.ShadowActionAvailable(prisoners));

		scn.ShadowUseCardAction(prisoners);

		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertInZone(Zone.REMOVED, guard);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsHindered(prisoners));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilASauronMinionIsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prisoners = scn.GetShadowCard("prisoners1");
		var slayer = scn.GetShadowCard("slayer");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCardsToDeadPile(guard);
		scn.MoveCardsToSupportArea(prisoners);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, troop, savage);
		// Vitality 2 with 1 wound: the only [sauron] minion is exhausted, beside a [moria] Orc and an [isengard]
		// Uruk-hai who could both exert.
		scn.AddWoundsToChar(slayer, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(prisoners));

		scn.RemoveWoundsFromChar(slayer, 1);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(prisoners));
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilACompanionIsInTheFreePeoplesPlayersDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var prisoners = scn.GetShadowCard("prisoners1");
		var swordsman = scn.GetShadowCard("swordsman");
		var shadowGuard = scn.GetShadowCard("guard1");
		var sam = scn.GetFreepsCard("sam");
		var merchant = scn.GetFreepsCard("merchant");
		var knight = scn.GetFreepsCard("knight1");
		var guard = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(sam);
		// An ally in the Free Peoples player's dead pile, a companion in his discard pile, and a companion in the
		// Shadow player's own dead pile.
		scn.MoveCardsToDeadPile(merchant, shadowGuard);
		scn.MoveCardsToDiscard(knight);
		scn.MoveCardsToSupportArea(prisoners);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(prisoners));

		scn.MoveCardsToDeadPile(guard);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(prisoners));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
