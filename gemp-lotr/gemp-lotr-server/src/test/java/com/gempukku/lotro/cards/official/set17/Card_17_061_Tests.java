package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("weaponmaster", "17_61");
					put("weaponmaster2", "17_61");

					put("corps", "11_73");      // Corps of Harad: a [men] Man; fierce only at a battleground (none on this path)
					put("squad", "11_99");      // Squad of Haradrim: a second [men] Man, of another title; a regroup ability never used here
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] minion who is no Man (Wizard); +2 to [men] minions bearing a possession, so kept off every board that reads a strength
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man (strength +3 and fierce only at 2 burdens)
					put("hillsman", "17_50");   // Stampeding Hillsman: a [men] Man, stacked and so not in play
					put("armor", "17_41");      // Ceremonial Armor: [men] possession in the support area, to hold the stacked Hillsman
					put("brand", "17_47");      // Primitive Brand: [men] hand weapon, "Bearer must be a [men] minion", strength +2; its trigger is on its bearer's win
					put("wait", "13_95");       // Lying in Wait: a [men] condition its skirmish action puts on a [men] minion; bearer +1 for each OTHER [men] minion
					// Armor: a [gondor] Free Peoples possession, "Bearer must be a Man".  Hemmed In 12_70's maneuver ability
					// transfers a Free Peoples possession from a companion to another eligible bearer, so a [men] Man can bear it.
					put("gondorarmor", "1_92");

					put("merry", "1_303");      // Merry, From O'er the Brandywine: a second [shire] companion (+2 only while bearing a weapon)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("walkers", "1_79");     // The Nine Walkers: a [gandalf] condition in the support area, with no [gandalf] character in play
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a [gollum] SHADOW card (a regroup ability only)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SunlandWeaponmasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Sunland Weaponmaster
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot 2 [men] Men.<br>While this minion bears a possession, he is strength +2 for each Free Peoples culture you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("weaponmaster");

		assertEquals("Sunland Weaponmaster", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilASecondMenManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weaponmaster = scn.GetShadowCard("weaponmaster");
		var corps = scn.GetShadowCard("corps");
		var squad = scn.GetShadowCard("squad");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var hillsman = scn.GetShadowCard("hillsman");
		var armor = scn.GetShadowCard("armor");

		scn.MoveCardsToHand(weaponmaster, squad);
		scn.StartGame();
		// One [men] Man in play; beside him a [men] minion who is no Man, a Man of another culture, and a [men] Man
		// stacked on a possession, out of play.
		scn.MoveMinionsToTable(corps, saruman, axeman);
		scn.MoveCardsToSupportArea(armor);
		scn.StackCardsOn(armor, hillsman);
		assertInZone(Zone.SUPPORT, armor);
		assertInZone(Zone.STACKED, hillsman);
		assertInHand(weaponmaster, squad);
		// Enough for both plays: 3 + 2 roaming, and 6 + 2 roaming.
		scn.SetTwilight(20);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(weaponmaster));

		assertTrue(scn.ShadowPlayAvailable(squad));
		scn.ShadowPlayCard(squad);
		assertInZone(Zone.SHADOW_CHARACTERS, squad);

		assertTrue(scn.ShadowPlayAvailable(weaponmaster));
		scn.ShadowPlayCard(weaponmaster);
		assertInZone(Zone.SHADOW_CHARACTERS, weaponmaster);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearingAPossessionHeIsPlusTwoForEachFreePeoplesCultureAsTheyAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weaponmaster = scn.GetShadowCard("weaponmaster");
		var weaponmaster2 = scn.GetShadowCard("weaponmaster2");
		var brand = scn.GetShadowCard("brand");
		var gollum = scn.GetShadowCard("gollum");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var walkers = scn.GetFreepsCard("walkers");

		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(aragorn);
		scn.StartGame();
		// Minions laid in the fellowship phase, to stand where they will stand in the Shadow phase (Q-0228).
		scn.MoveMinionsToTable(weaponmaster, weaponmaster2, gollum);
		scn.AttachCardsTo(weaponmaster, brand);
		assertAttachedTo(brand, weaponmaster);
		assertInHand(aragorn);
		scn.FreepsRefreshDecision();

		// [shire] alone (Frodo and Merry), and the [gollum] culture of a Shadow card is no Free Peoples culture:
		// 13 + 2 for the Brand + 2.  The second copy bears nothing.
		assertEquals(17, scn.GetStrength(weaponmaster));
		assertEquals(13, scn.GetStrength(weaponmaster2));

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		// [shire] and [gondor]: 13 + 2 + 4
		assertEquals(19, scn.GetStrength(weaponmaster));
		assertEquals(13, scn.GetStrength(weaponmaster2));

		// A culture spotted on a card that is no character: 13 + 2 + 6
		scn.MoveCardsToSupportArea(walkers);
		assertInZone(Zone.SUPPORT, walkers);
		assertEquals(21, scn.GetStrength(weaponmaster));
		assertEquals(13, scn.GetStrength(weaponmaster2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AsAPossessionIsPlayedOnHimTheBonusSwitchesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weaponmaster = scn.GetShadowCard("weaponmaster");
		var weaponmaster2 = scn.GetShadowCard("weaponmaster2");
		var brand = scn.GetShadowCard("brand");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(brand);
		scn.StartGame();
		scn.MoveMinionsToTable(weaponmaster, weaponmaster2);
		assertInHand(brand);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(13, scn.GetStrength(weaponmaster));
		assertEquals(13, scn.GetStrength(weaponmaster2));

		scn.ShadowPlayCard(brand);
		assertTrue(scn.ShadowHasCardChoiceAvailable(weaponmaster, weaponmaster2));
		scn.ShadowChooseCard(weaponmaster);
		assertAttachedTo(brand, weaponmaster);

		// [shire] and [gondor]: 13 + 2 for the Brand + 4.  The other copy bears nothing.
		assertEquals(19, scn.GetStrength(weaponmaster));
		assertEquals(13, scn.GetStrength(weaponmaster2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearingAConditionIsNotEnoughButAFreePeoplesPossessionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weaponmaster = scn.GetShadowCard("weaponmaster");
		var wait = scn.GetShadowCard("wait");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gondorarmor = scn.GetFreepsCard("gondorarmor");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.MoveMinionsToTable(weaponmaster);
		assertEquals(13, scn.GetStrength(weaponmaster));

		// A [men] condition he bears; its own bonus counts the OTHER [men] minions, and there are none.
		scn.AttachCardsTo(weaponmaster, wait);
		assertAttachedTo(wait, weaponmaster);
		assertEquals(13, scn.GetStrength(weaponmaster));

		// A possession of another culture and side: [shire] and [gondor], the Armor's own culture already counted.
		scn.AttachCardsTo(weaponmaster, gondorarmor);
		assertAttachedTo(gondorarmor, weaponmaster);
		assertEquals(17, scn.GetStrength(weaponmaster));
	}
}
