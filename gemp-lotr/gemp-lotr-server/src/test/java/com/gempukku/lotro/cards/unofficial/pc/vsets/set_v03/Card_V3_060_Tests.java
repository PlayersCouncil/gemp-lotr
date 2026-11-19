package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mouth", "103_60");
					put("hunt", "1_126");        // Hunt Them Down!: [isengard] Maneuver event, cost 3; makes an Uruk-hai fierce, and asks for nothing
					put("mumakil", "7_153");     // Mumakil of the Harad: [raider] Maneuver event, cost 1; spot a [raider] minion to add (2) for each threat
					put("explorer1", "4_250");   // Southron Explorer: [raider] Man, strength 5, vitality 3; a regroup ability never used here
					put("explorer2", "4_250");
					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("slayer", "3_93");       // Morgul Slayer: [sauron] Orc, vitality 2. Regroup: exert this minion to wound a companion (except the Ring-bearer)
					put("halberd", "7_156");     // Raider Halberd: Shadow hand weapon, strength +2, bearer must be a [raider] Man; a skirmish ability never used here

					put("gandalf", "1_72");      // Gandalf, Friend of the Shirefolk: vitality 4
					put("mithrandir", "4_98");   // Mithrandir, Mithrandir!: [gandalf] Maneuver event, cost 3; exert Gandalf to wound each minion of strength 6 or less
					put("glamdring", "1_75");    // Glamdring: [gandalf] possession, hand weapon, borne by Gandalf
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4, a ranger
					put("anduril", "7_80");      // Anduril, King's Blade: [gondor] artifact, hand weapon, borne by Aragorn
					put("bow", "7_94");          // Gondor Bow: [gondor] possession, ranged weapon; a skirmish ability never used here
					put("armor", "1_92");        // Armor: [gondor] possession that is no weapon; bearer takes no more than 1 wound in a skirmish
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMouthofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Mouth of Sauron, Herald of the Dark Lord
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Lurker.
		 * 	Your Shadow events are twilight cost -2. Response: If a minion is about to take a wound, exert this
		 * 	minion to prevent that wound and hinder a Free Peoples weapon.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mouth");

		assertEquals("The Mouth of Sauron", card.getBlueprint().getTitle());
		assertEquals("Herald of the Dark Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForThreeAndTwoMoreWhileRoamingAndOnlyYourShadowEventsThenCostTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var hunt = scn.GetShadowCard("hunt");
		var mumakil = scn.GetShadowCard("mumakil");
		var explorer = scn.GetShadowCard("explorer1");
		var shadowMithrandir = scn.GetShadowCard("mithrandir");
		var freepsHunt = scn.GetFreepsCard("hunt");
		var freepsMithrandir = scn.GetFreepsCard("mithrandir");
		scn.MoveCardsToHand(mouth, hunt, mumakil, explorer, shadowMithrandir);
		scn.MoveCardsToHand(freepsHunt, freepsMithrandir);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 2 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(5, scn.GetTwilight());
		assertEquals(3, scn.GetTwilightCost(hunt));
		assertEquals(1, scn.GetTwilightCost(mumakil));
		assertInZone(Zone.HAND, mouth);
		assertTrue(scn.ShadowPlayAvailable(mouth));

		scn.ShadowPlayCard(mouth);

		// 5 - 3 to play - 2 roaming (a site 4 minion played at site 2)
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, mouth);

		// His owner's Shadow events, of his culture or another: 3 - 2, and 1 - 2 is no less than nothing.
		assertEquals(1, scn.GetTwilightCost(hunt));
		assertEquals(0, scn.GetTwilightCost(mumakil));
		// His owner's Shadow card that is no event, and his owner's Free Peoples event.
		assertEquals(2, scn.GetTwilightCost(explorer));
		assertEquals(3, scn.GetTwilightCost(shadowMithrandir));
		// The other player's Shadow event and Free Peoples event.
		assertEquals(3, scn.GetTwilightCost(freepsHunt));
		assertEquals(3, scn.GetTwilightCost(freepsMithrandir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowEventsArePlayedForTwoLessAndAFreePeoplesEventForItsPrintedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var hunt = scn.GetShadowCard("hunt");
		var mumakil = scn.GetShadowCard("mumakil");
		var gandalf = scn.GetFreepsCard("gandalf");
		var mithrandir = scn.GetFreepsCard("mithrandir");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(mithrandir);
		scn.MoveCardsToHand(hunt, mumakil);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);
		scn.FreepsPass();

		// An empty pool: the event printed at 1 can be played and the event printed at 3 cannot.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(mumakil));
		assertFalse(scn.ShadowPlayAvailable(hunt));

		// No threats: the event itself adds nothing.
		scn.ShadowPlayCard(mumakil);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, mumakil);

		// The Free Peoples player's event adds its printed 3. The Mouth, strength 9, is not wounded by it.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(mithrandir);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(mouth));

		// 3 - 2 = 1 to play
		assertTrue(scn.ShadowPlayAvailable(hunt));
		scn.ShadowPlayCard(hunt);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, hunt);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhileHeIsHinderedShadowEventsCostTheirPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var explorer = scn.GetShadowCard("explorer1");
		var hunt = scn.GetShadowCard("hunt");
		var mumakil = scn.GetShadowCard("mumakil");
		scn.MoveCardsToHand(hunt, mumakil);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, explorer);
		scn.HinderCard(mouth);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 in the pool: enough for the event printed at 3 only if it cost 2 less.
		assertTrue(scn.IsHindered(mouth));
		assertEquals(2, scn.GetTwilight());
		assertEquals(3, scn.GetTwilightCost(hunt));
		assertFalse(scn.ShadowPlayAvailable(hunt));
		assertTrue(scn.ShadowPlayAvailable(mumakil));

		// The Explorer is the [raider] minion spotted; no threats, so nothing is added.
		scn.ShadowPlayCard(mumakil);

		// 2 - 1 to play
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachUsePreventsOneMinionsWoundAndHindersAFreePeoplesWeaponOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var savage = scn.GetShadowCard("savage");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var halberd = scn.GetShadowCard("halberd");
		var gandalf = scn.GetFreepsCard("gandalf");
		var mithrandir = scn.GetFreepsCard("mithrandir");
		var glamdring = scn.GetFreepsCard("glamdring");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var bow = scn.GetFreepsCard("bow");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.AttachCardsTo(aragorn, anduril, bow, armor);
		scn.MoveCardsToHand(mithrandir);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, savage, explorer1, explorer2);
		scn.AttachCardsTo(explorer2, halberd);
		scn.SkipToPhase(Phase.MANEUVER);

		// The event wounds each minion of strength 6 or less: the Savage (5) and the first Explorer (5), not
		// the Mouth (9) and not the Explorer with the Halberd (5 + 2). Each wound is answered by itself, in
		// an order the game does not fix.
		assertEquals(7, scn.GetStrength(explorer2));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(explorer1));

		scn.FreepsPlayCard(mithrandir);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mouth));
		scn.ShadowChooseOptionalTrigger(mouth);

		assertEquals(1, scn.GetWoundsOn(mouth));
		// Which weapon: a possession or an artifact, a hand weapon or a ranged weapon, on either companion;
		// not a Free Peoples possession that is no weapon, and not a Shadow weapon.
		assertTrue(scn.ShadowHasCardChoiceAvailable(glamdring, anduril, bow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armor, halberd));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(glamdring);

		assertTrue(scn.IsHindered(glamdring));
		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(bow));

		// The other minion, of another culture, is still about to take its wound, and the Mouth can exert
		// once more.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mouth));
		scn.ShadowChooseOptionalTrigger(mouth);

		assertEquals(2, scn.GetWoundsOn(mouth));
		// The weapon hindered before is not offered again.
		assertTrue(scn.ShadowHasCardChoiceAvailable(anduril, bow));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(glamdring, armor, halberd));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bow);

		assertTrue(scn.IsHindered(bow));
		assertFalse(scn.IsHindered(anduril));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(halberd));
		// Neither wound was taken. The two minions the event did not reach were never asked about.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(explorer1));
		assertEquals(0, scn.GetWoundsOn(explorer2));
		assertEquals(2, scn.GetWoundsOn(mouth));
		assertInZone(Zone.DISCARD, mithrandir);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void PreventsHisOwnWoundWhenHeIsTheMinionAboutToTakeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mouth);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn with Anduril, 8 + 2, beats the Mouth, 9, who is about to take one wound.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(mouth));

		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mouth));
		scn.ShadowChooseOptionalTrigger(mouth);

		assertTrue(scn.ShadowHasCardChoiceAvailable(anduril, bow));
		scn.ShadowChooseCard(bow);

		// He has his exertion and not the wound as well.
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertTrue(scn.IsHindered(bow));
		assertFalse(scn.IsHindered(anduril));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesTheWoundToBeTakenAndWithNoFreePeoplesWeaponAUseStillPreventsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");
		var halberd = scn.GetShadowCard("halberd");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, aragorn);
		// A Free Peoples possession that is no weapon, and a Shadow weapon: nothing here can be hindered.
		scn.AttachCardsTo(aragorn, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, explorer1, explorer2);
		scn.AttachCardsTo(explorer2, halberd);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, explorer1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(explorer1));
		assertEquals(0, scn.GetWoundsOn(mouth));

		// Gimli (6, damage +1) beats the Explorer (5), who is about to take two wounds, one at a time.
		scn.PassSkirmishActions();

		// The first is not answered, and is taken.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mouth));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(explorer1));
		assertEquals(0, scn.GetWoundsOn(mouth));

		// The second is answered, and is not.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mouth));
		scn.ShadowChooseOptionalTrigger(mouth);

		assertEquals(1, scn.GetWoundsOn(mouth));
		assertEquals(1, scn.GetWoundsOn(explorer1));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(halberd));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsOneWoundOfTwoAndIsNotOfferedOnceHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var explorer1 = scn.GetShadowCard("explorer1");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, explorer1);
		// Vitality 3 with 1 wound: he can exert once more.
		scn.AddWoundsToChar(mouth, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, explorer1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(explorer1));
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertFalse(scn.IsHindered(anduril));

		// Gimli (6, damage +1) beats the Explorer (5), who is about to take two wounds, one at a time.
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mouth));
		scn.ShadowChooseOptionalTrigger(mouth);

		// The first wound is prevented and the only Free Peoples weapon hindered. The second finds the Mouth
		// exhausted: it is not answered, and is taken.
		assertEquals(2, scn.GetWoundsOn(mouth));
		assertTrue(scn.IsExhausted(mouth));
		assertTrue(scn.IsHindered(anduril));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(explorer1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionExertingAndACompanionAboutToTakeAWoundAreNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var slayer = scn.GetShadowCard("slayer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth, slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowActionAvailable(slayer));

		// The Slayer exerts, and Aragorn (the only companion who is not the Ring-bearer) takes a wound.
		scn.ShadowUseCardAction(slayer);

		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertFalse(scn.IsHindered(anduril));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
