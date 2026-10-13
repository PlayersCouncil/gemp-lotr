package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_100_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sauron", "103_100");
					put("oldsauron", "9_48");     // Sauron, The Lord of the Rings: cost 18, less 1 for each burden, threat and controlled site
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
					put("swordsman", "3_98");     // Orc Swordsman: [sauron] Orc, strength 9; stronger while he bears a weapon
					put("throne", "17_105");      // Throne of the Dark Lord: Shadow artifact, support area; minions other than Sauron are damage +1 beside him
					put("prisoners", "103_99");   // Release the Prisoners!: Shadow condition with a maneuver ability
					put("coat", "2_105");         // Mithril-coat: artifact borne by Frodo; minion archery -1
					put("shards", "3_44");        // The Shards of Narsil: Free Peoples artifact in the support area; fellowship abilities
					put("sword", "1_299");        // Hobbit Sword: possession, strength +2
					put("bilbo", "1_284");        // Bilbo, Retired Adventurer: ally with a fellowship ability
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: ally, strength 8, vitality 4, home site 3
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_97");       // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability
					put("knight", "5_35");        // Gondorian Knight: strength 6, vitality 3; knight and nothing else
					put("guard1", "1_7");         // Dwarf Guard: strength 4, vitality 2; to play, spot a Dwarf
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sauron, Lord of All Middle-earth
		 * Unique: true
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 18
		 * Type: Minion
		 * Subtype: Maia
		 * Strength: 24
		 * Vitality: 5
		 * Site Number: 6
		 * Game Text: Sauron is twilight cost -1 for each artifact and companion you can spot (limit -6).
		 * 		While you can spot 6 companions, Sauron is <b>fierce</b>.  While you can spot 7 companions, Sauron is
		 * 		<b>relentless</b> <i>(he participates in 1 extra round of skirmishes after fierce.)</i>.
		 * 		Each time a companion loses a skirmish to Sauron, kill that companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sauron");

		assertEquals("Sauron", card.getBlueprint().getTitle());
		assertEquals("Lord of All Middle-earth", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAIA, card.getBlueprint().getRace());
		assertEquals(18, card.getBlueprint().getTwilightCost());
		assertEquals(24, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertFalse(scn.HasKeyword(card, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(card, Keyword.RELENTLESS));
	}

	@Test
	public void IsPlayedForEighteenLessOneForEachArtifactAndCompanionAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var throne = scn.GetShadowCard("throne");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var coat = scn.GetFreepsCard("coat");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(frodo, coat);
		scn.MoveCardsToSupportArea(throne);
		scn.MoveCardsToHand(sauron);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 3 companions + 2 for the site
		assertEquals(15, scn.GetTwilight());
		// 18 - 3 companions - 2 artifacts; the ally takes nothing off.
		assertEquals(13, scn.GetTwilightCost(sauron));
		assertTrue(scn.ShadowPlayAvailable(sauron));

		scn.ShadowPlayCard(sauron);

		// 13 to play + 2 roaming: his site number is 6 and the fellowship is at site 2.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, sauron);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CostsOneLessForEachArtifactOfEitherSideAndEachCompanionDownToTwelve() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var oldsauron = scn.GetShadowCard("oldsauron");
		var troop = scn.GetShadowCard("troop");
		var throne = scn.GetShadowCard("throne");
		var prisoners = scn.GetShadowCard("prisoners");
		var frodo = scn.GetRingBearer();
		var coat = scn.GetFreepsCard("coat");
		var shards = scn.GetFreepsCard("shards");
		var sword = scn.GetFreepsCard("sword");
		var bilbo = scn.GetFreepsCard("bilbo");
		var knight = scn.GetFreepsCard("knight");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		scn.MoveCardsToHand(sauron, oldsauron, knight);

		scn.StartGame();

		// The Ring-bearer is 1 companion; The One Ring he bears is no artifact.
		assertEquals(17, scn.GetTwilightCost(sauron));
		// Another Sauron beside him in hand costs what is printed on it.
		assertEquals(18, scn.GetTwilightCost(oldsauron));

		// An ally, a possession, a minion and a condition take nothing off.
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(prisoners);
		assertEquals(17, scn.GetTwilightCost(sauron));

		// A Free Peoples artifact a companion bears, one in the support area, and a Shadow artifact.
		scn.AttachCardsTo(frodo, coat);
		assertEquals(16, scn.GetTwilightCost(sauron));
		scn.MoveCardsToSupportArea(shards);
		assertEquals(15, scn.GetTwilightCost(sauron));
		scn.MoveCardsToSupportArea(throne);
		assertEquals(14, scn.GetTwilightCost(sauron));

		// A second companion, played from hand: 18 - 2 companions - 3 artifacts.
		scn.FreepsPlayCard(knight);
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertEquals(13, scn.GetTwilightCost(sauron));
		assertEquals(18, scn.GetTwilightCost(oldsauron));

		// 3 companions and 3 artifacts: the limit of -6 is reached.
		scn.MoveCompanionsToTable(guard1);
		assertEquals(12, scn.GetTwilightCost(sauron));
		// 4 companions and 3 artifacts, then 5 and 3: no further.
		scn.MoveCompanionsToTable(guard2);
		assertEquals(12, scn.GetTwilightCost(sauron));
		scn.MoveCompanionsToTable(guard3);
		assertEquals(12, scn.GetTwilightCost(sauron));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsFierceOnceASixthCompanionIsPlayedAndRelentlessOnceASeventhIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var coat = scn.GetFreepsCard("coat");
		var bilbo = scn.GetFreepsCard("bilbo");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		var guard5 = scn.GetFreepsCard("guard5");
		// 5 companions with the Ring-bearer, and beside them an ally and an artifact, which count toward his cost
		// and not toward this.
		scn.MoveCompanionsToTable(aragorn, guard1, guard2, guard3);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(frodo, coat);
		scn.MoveCardsToHand(guard4, guard5);

		scn.StartGame();
		scn.MoveMinionsToTable(sauron, troop);

		assertFalse(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sauron, Keyword.RELENTLESS));

		scn.FreepsPlayCard(guard4);
		assertInZone(Zone.FREE_CHARACTERS, guard4);
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sauron, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		scn.FreepsPlayCard(guard5);
		assertInZone(Zone.FREE_CHARACTERS, guard5);
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(sauron, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.RELENTLESS));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void KillsACompanionWhoLosesASkirmishToHimAndNotOneWhoLosesToAnotherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var swordsman = scn.GetShadowCard("swordsman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		// Aragorn at 8 + 5 = 13 loses to Sauron's 24 without being overwhelmed (26 would be needed), and with
		// vitality 4 the wound for losing does not kill him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Aragorn"), null, 5));

		scn.StartGame();
		scn.MoveMinionsToTable(sauron, swordsman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, sauron }, new PhysicalCardImpl[] { knight, swordsman });

		// The Knight, 6, loses to the Orc Swordsman, 9, with Sauron in play.
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertInZone(Zone.FREE_CHARACTERS, knight);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, sauron));
		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		scn.PassSkirmishActions();

		// The wound for losing and Sauron's text answer the same loss; the wound is placed first here.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, knight, frodo);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DoesNotKillAnAllyWhoLosesASkirmishToHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);
		// Elrond at 8 + 5 = 13 loses to Sauron's 24 without being overwhelmed.
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Elrond"), null, 5));

		scn.StartGame();
		// Elrond's home is site 3: the fellowship moves there from site 2.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(sauron);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, sauron);
		scn.FreepsResolveSkirmish(elrond);
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, sauron));
		assertEquals(13, scn.GetStrength(elrond));
		assertEquals(0, scn.GetWoundsOn(elrond));

		scn.PassSkirmishActions();

		// The wound for losing, and no more.
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertInZone(Zone.SUPPORT, elrond);
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void KillsASecondCompanionInTheFierceSkirmishAndIsFierceNoLongerWithFiveLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sauron = scn.GetShadowCard("sauron");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");
		// 7 companions with the Ring-bearer.
		scn.MoveCompanionsToTable(aragorn, boromir, guard1, guard2, guard3, guard4);
		// Aragorn at 8 + 5 and Boromir at 7 + 6 are both 13: beaten by 24, not overwhelmed, not killed by one wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Aragorn"), null, 5));
		scn.ApplyAdHocModifier(new StrengthModifier(null, Filters.name("Boromir"), null, 6));

		scn.StartGame();
		scn.MoveMinionsToTable(sauron);
		scn.SkipToAssignments();

		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(sauron, Keyword.RELENTLESS));

		scn.FreepsAssignToMinions(aragorn, sauron);
		scn.FreepsResolveSkirmish(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		scn.PassSkirmishActions();

		// Sauron's text is carried out before the wound for losing this time, and after it in the fierce skirmish.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Sauron");

		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, boromir, frodo, guard1, guard2, guard3, guard4);
		// 6 companions are left.
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sauron, Keyword.RELENTLESS));

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, sauron);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(13, scn.GetStrength(boromir));
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassFierceSkirmishActions();

		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DEAD, aragorn, boromir);
		assertInZone(Zone.FREE_CHARACTERS, frodo, guard1, guard2, guard3, guard4);
		// 5 companions are left.
		assertFalse(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sauron, Keyword.RELENTLESS));
		assertEquals(0, scn.GetWoundsOn(sauron));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
