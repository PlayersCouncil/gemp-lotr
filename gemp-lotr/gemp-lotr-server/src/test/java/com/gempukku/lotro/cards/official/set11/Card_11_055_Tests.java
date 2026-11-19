package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "11_55");

					// [gondor] Men of each type, and characters who fail one word of "a [gondor] Man".
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6; "Knight." is his whole text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: a [rohan] Man ally; a regroup ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose

					// Damage +1 minions of two cultures and races, and a third for another skirmish.
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; Damage +1 is its whole text
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll, strength 15; Damage +1 and fierce; its other text is about being played
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8; Damage +1 is its whole text
					put("lurtzcry", "1_128");   // Lurtz's Battle Cry: Skirmish event, "Make an Uruk-hai strength -1 and damage +1"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 4 is White Rocks 4_346, a battleground (its Shadow number counts mounted companions).
	// Site 3, the home of the Gondorian Merchant (3T), is Barrows of Edoras 4_337, a sanctuary and no
	// battleground.  The Merchant is the only [gondor] ally, and no ally's home is a battleground.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "11_55");
					put("armor2", "11_55");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; "Knight." is his whole text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, home 3T; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 4 is White Rocks 4_346, a battleground; site 1, Eastemnet Gullies 4_325, and site 3,
	// Barrows of Edoras 4_337, are none.  Site 3 is the Gondorian Merchant's home (3T), where he skirmishes.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "11_55");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; "Knight." is his whole text
					put("knight2", "5_35");
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, strength 6, home 3T; a maneuver ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmoroftheCitadelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Armor of the Citadel
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a [gondor] Man.<br>Each minion skirmishing bearer loses all <b>damage</b> bonuses.<br>While bearer is at a battleground site and unwounded, he or she is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armor");

		assertEquals("Armor of the Citadel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BorneByAnAllyWhoseHomeIsNoBattlegroundItGivesNoDefenderWithTheFellowshipAtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(knight, armor);
		scn.AttachCardsTo(merchant, armor2);

		scn.StartGame();
		scn.SkipToSite(4);
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(merchant));

		// Both unwounded.  The companion is with the fellowship, at the battleground; the Merchant is at his home
		// site, Barrows of Edoras (CR 4.2 "ally").
		assertTrue(scn.HasKeyword(knight, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(merchant, Keyword.DEFENDER));
	}

	@Test
	public void CanBePlayedOnAGondorManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var armor = scn.GetFreepsCard("armor");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(knight, pippin);
		scn.MoveAlliesToTable(merchant, herdsman);
		scn.MoveCardsToHand(armor);
		assertInHand(armor);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);

		// A [gondor] Man companion and a [gondor] Man ally; not the [gondor] Hobbit, the [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, herdsman, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(armor, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionSkirmishingBearerLosesAllDamageBonusesAndOneSkirmishingAnotherCompanionKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetShadowCard("guard");
		var troll = scn.GetShadowCard("troll");
		var sentry = scn.GetShadowCard("sentry");
		var lurtzcry = scn.GetShadowCard("lurtzcry");
		scn.MoveCompanionsToTable(knight, aragorn);
		scn.AttachCardsTo(knight, armor);
		scn.MoveCardsToHand(lurtzcry);
		assertAttachedTo(armor, knight);
		assertInHand(lurtzcry);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(guard, troll, sentry);

		// The Guard and the Troll both go to the Knight, the Troll put there by the Shadow player; the Sentry to Aragorn.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, guard}, new PhysicalCardImpl[]{aragorn, sentry});
		scn.ShadowAssignToMinions(knight, troll);

		// Assigned to the bearer but not yet skirmishing him: every bonus still there.
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		// In Aragorn's skirmish, fought first: the Sentry skirmishes another companion and keeps its bonus, and the
		// two waiting for the Knight keep theirs.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, sentry));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		// In the Knight's skirmish, both minions lose their bonus.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishingAgainst(knight, guard));
		assertTrue(scn.IsCharSkirmishingAgainst(knight, troll));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		// "All": a bonus given during the skirmish is lost too, while the rest of the event lands
		// (CR 4.2 "damage bonus": a character that loses all damage bonuses cannot gain one).
		assertEquals(9, scn.GetStrength(guard));
		scn.FreepsPass();
		scn.ShadowPlayCard(lurtzcry);
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, sentry));
		scn.ShadowChooseCard(guard);
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BorneByAnAllyAtHisHomeSiteTheMinionSkirmishingHimLosesItsDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var armor = scn.GetFreepsCard("armor");
		var merchant = scn.GetFreepsCard("merchant");
		var savage = scn.GetShadowCard("savage");
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, armor);
		assertAttachedTo(armor, merchant);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();

		// Site 3 of the Towers block, Barrows of Edoras: the Merchant is at his home and may skirmish.
		assertEquals("Barrows of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.FreepsCanAssign(merchant));
		scn.FreepsAssignToMinions(merchant, savage);
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(merchant);
		assertTrue(scn.IsCharSkirmishingAgainst(merchant, savage));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerIsDefenderPlus1OnlyWhileAtABattlegroundAndUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var armor = scn.GetFreepsCard("armor");
		var knight = scn.GetFreepsCard("knight");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(knight, knight2);
		scn.AttachCardsTo(knight, armor);
		assertAttachedTo(armor, knight);

		scn.StartGame();
		// Eastemnet Gullies, no battleground.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DEFENDER));

		scn.SkipToSite(3);
		assertEquals("Barrows of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DEFENDER));

		scn.SkipToSite(4);
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(knight2));
		// The bearer, unwounded at a battleground; not the other Knight, who bears no armor.
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(knight2, Keyword.DEFENDER));

		// One wound: no longer unwounded.
		scn.AddWoundsToChar(knight, 1);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
