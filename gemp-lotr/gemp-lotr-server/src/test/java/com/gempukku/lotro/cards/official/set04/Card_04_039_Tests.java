package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("raid", "4_39");

					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, no text
					put("warrior", "4_18");     // Dunlending Warrior: [dunland] Man; an assignment ability that needs an ally
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] Wizard, no Man; his move trigger is laid out of the way
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man; stronger and fierce only at 2 burdens
					put("poison", "8_28");      // Spider Poison: a Shadow possession in its owner's support area; its ability needs Shelob
					put("breath", "1_207");     // Black Breath: a Shadow condition in its owner's support area; its ability is a skirmish one

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("armor", "1_92");       // Armor: [gondor] possession borne by a Man; caps his wounds in skirmishes only
					put("saga", "1_114");       // The Saga of Elendil: [gondor] condition borne by a [gondor] companion; a skirmish ability
					put("toby", "1_305");       // Old Toby: [shire] possession in the support area; its draw is offered only as it is played
					put("moria", "1_21");       // Lord of Moria: [dwarven] condition in the support area; makes Dwarves damage +1
					put("shards", "3_44");      // The Shards of Narsil: a Free Peoples ARTIFACT in the support area; fellowship abilities only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WildManRaidStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Wild Man Raid
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot 3 [dunland] Men to discard a Free Peoples possession or condition.
		*/

		var scn = GetScenario();

		var raid = scn.GetFreepsCard("raid");

		assertEquals("Wild Man Raid", raid.getBlueprint().getTitle());
		assertNull(raid.getBlueprint().getSubtitle());
		assertFalse(raid.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, raid.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, raid.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, raid.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(raid, Timeword.MANEUVER));
		assertEquals(1, raid.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithThreeDunlandMenItDiscardsAFreePeoplesPossessionOrConditionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raid = scn.GetShadowCard("raid");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var poison = scn.GetShadowCard("poison");
		var breath = scn.GetShadowCard("breath");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var saga = scn.GetFreepsCard("saga");
		var toby = scn.GetFreepsCard("toby");
		var moria = scn.GetFreepsCard("moria");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor, saga);
		scn.MoveCardsToSupportArea(toby, moria, shards);
		scn.MoveCardsToHand(raid);
		assertAttachedTo(armor, aragorn);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, toby, moria, shards);
		assertInHand(raid);

		scn.StartGame();
		// Three [dunland] Men of three titles: exactly the three the text asks for.
		scn.MoveMinionsToTable(brigand, savage, warrior);
		scn.MoveCardsToSupportArea(poison, breath);
		assertInZone(Zone.SUPPORT, poison, breath);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(raid));
		scn.ShadowPlayCard(raid);

		// Free Peoples possessions and conditions, borne and in the support area, of four cultures.  Not the Free
		// Peoples artifact, nor the Shadow possession or condition, nor any character.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, toby, saga, moria));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shards, poison, breath, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(armor);

		assertInDiscard(armor, raid);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, toby, moria, shards, poison, breath);
		assertInPlay(aragorn, frodo, brigand, savage, warrior);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTwoDunlandMenBesideADunlandMinionWhoIsNoManAndAManOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raid = scn.GetShadowCard("raid");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToHand(raid);
		assertAttachedTo(armor, aragorn);
		assertInHand(raid);

		scn.StartGame();
		// Laid once the fellowship has moved, so that Saruman's move trigger has nothing to answer.
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, savage, saruman, axeman);
		assertInPlay(brigand, savage, saruman, axeman);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// 2 companions + 2 for the site: the event could be paid for.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(raid));

		assertInHand(raid);
		assertAttachedTo(armor, aragorn);
	}

	@Test
	public void WithNothingItCanDiscardItIsStillPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var raid = scn.GetShadowCard("raid");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var poison = scn.GetShadowCard("poison");
		var breath = scn.GetShadowCard("breath");
		var frodo = scn.GetRingBearer();
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(raid);
		assertInZone(Zone.SUPPORT, shards);
		assertInHand(raid);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, savage, warrior);
		scn.MoveCardsToSupportArea(poison, breath);
		assertInZone(Zone.SUPPORT, poison, breath);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(raid));
		scn.ShadowPlayCard(raid);

		assertInDiscard(raid);
		assertInZone(Zone.SUPPORT, shards, poison, breath);
		assertInPlay(frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
