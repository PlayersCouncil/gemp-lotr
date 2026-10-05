package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_018_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("council", "101_18");

					// Gandalf, Friend of the Shirefolk: his signet is Frodo's, so he is healed as Gandalf and
					// not for his signet.  His own text only changes his strength.
					put("gandalf", "1_72");
					// Aragorn, Ranger of the North: a companion with the Gandalf signet, resistance 6.  His
					// maneuver ability is never used.
					put("aragorn", "1_89");
					// Legolas, Greenleaf: an [elven] companion with the Frodo signet, and an archer, which makes
					// the fellowship archery total 1.  His archery ability is never used.
					put("legolas", "1_50");

					// [elven] allies, vitality 3 each.  Golradir's and Saelbeth's abilities are skirmish
					// actions and Celeborn's a fellowship action; none is used.
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: home site 3
					put("saelbeth", "3_25");    // Saelbeth, Elven Councilor: home site 3; to play, spot an Elf
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: home site 6
					// Albert Dreary, Entertainer from Bree: a [gandalf] ally, vitality 3.  His ability is a
					// maneuver action.
					put("albert", "1_69");

					// Every Saruman whose first sentence is "Saruman may not take wounds during the archery
					// phase and may not be assigned to a skirmish."
					put("keeper", "3_68");      // Saruman, Keeper of Isengard.  His second sentence: "Uruk-hai are fierce."
					put("servant", "3_69");     // Saruman, Servant of the Eye
					put("traitor", "4_173");    // Saruman, Black Traitor
					put("master", "5_56");      // Saruman, Master of Foul Folk
					put("keeperPC", "53_68");   // Saruman, Keeper of Isengard, as the Players' Council revised him
					// Saruman, Fell Voice, of this card's own set: the same, "while you cannot spot a weather".
					put("fellVoice", "101_30");
					// Every Saruman whose first sentence only keeps him out of skirmishes.  On the Rabble-rouser
					// and the Mind of Metal and Wheels it is "Saruman may not be assigned to a skirmish."; on
					// the Instigator of Insurrection, "Saruman cannot be assigned to a skirmish."
					put("rabble", "4_33");      // Saruman, Rabble-rouser: a [dunland] minion
					put("instigator", "17_37"); // Saruman, Instigator of Insurrection
					put("mind", "102_29");      // Saruman, Mind of Metal and Wheels
					// The three whose first sentences are something else.
					// Saruman, Master of the White Hand: an [uruk-hai] minion.  "Each companion is resistance -1."
					put("whiteHand", "17_116");
					// Saruman, Coldly Still: a [men] minion.  "Each [men] minion bearing a possession is strength +2."
					put("coldlyStill", "17_48");
					// Saruman, Curunir: an [uruk-hai] minion.  "Each time an [uruk-hai] hunter wins a skirmish,
					// heal a different [uruk-hai] minion."
					put("curunir", "17_115");

					put("axeman", "11_69");     // Axeman of Harad: a [men] minion, strength 11; his text is about his cost
					put("poleaxe", "11_95");    // Poleaxe: a [men] possession, strength +2; its ability is a skirmish action
					// Tracking Uruk: an [uruk-hai] Uruk-hai, strength 14, vitality 3, hunter 1 and nothing else.
					put("tracker1", "15_171");
					put("tracker2", "15_171");
					put("tracker3", "15_171");

					// Gandalf, Leader of Dwarves, of the Hobbit sets: a Gandalf who is not Saruman, and whose
					// first ability the game can switch off.  The Hobbit sets have no sheet of card text, so
					// what his first sentence says is known only from his definition: each time his owner
					// reconciles, that player may draw 2 cards.  His other abilities are skirmish actions.
					put("hobbitGandalf", "30_25");

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai, strength 5, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,    // Frodo, Son of Drogo: the Frodo signet
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWhiteCouncilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: The White Council
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot Gandalf and 2 [elven] allies, the first sentence of Saruman's game
		 * text does not apply.
		 * Regroup: Exert 2 [elven] allies to heal Gandalf or a companion with the Gandalf signet (limit 2 per
		 * phase).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("council");

		assertEquals("The White Council", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		scn.MoveCardsToHand(council);

		scn.StartGame();

		assertInHand(council);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(council));

		scn.FreepsPlayCard(council);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, council);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SarumansFirstSentenceStopsApplyingWhenASecondElvenAllyIsPlayedBesideGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var whiteHand = scn.GetShadowCard("whiteHand");
		var coldlyStill = scn.GetShadowCard("coldlyStill");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(council);
		// Gandalf and one [elven] ally.  Beside them an [elven] companion and a [gandalf] ally, neither of
		// which is an [elven] ally.
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("legolas"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("golradir"), scn.GetFreepsCard("albert"));
		scn.MoveCardsToHand(saelbeth);

		scn.StartGame();

		scn.MoveMinionsToTable(axeman);
		scn.AttachCardsTo(axeman, scn.GetShadowCard("poleaxe"));
		assertEquals(6, scn.GetResistance(aragorn));
		// 11 + 2 for the Poleaxe
		assertEquals(13, scn.GetStrength(axeman));

		scn.MoveMinionsToTable(whiteHand, coldlyStill);
		// "Each companion is resistance -1": 6 - 1
		assertEquals(5, scn.GetResistance(aragorn));
		// "Each [men] minion bearing a possession is strength +2": 13 + 2
		assertEquals(15, scn.GetStrength(axeman));

		scn.FreepsPlayCard(saelbeth);
		assertInZone(Zone.SUPPORT, saelbeth);
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(13, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SarumansFirstSentenceStopsApplyingWhenGandalfIsPlayedBesideTwoElvenAllies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var whiteHand = scn.GetShadowCard("whiteHand");
		scn.MoveCardsToSupportArea(council);
		// Two [elven] allies and no Gandalf.  Beside them a [gandalf] ally, and the [gandalf] condition
		// itself.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(scn.GetFreepsCard("golradir"), scn.GetFreepsCard("saelbeth"), scn.GetFreepsCard("albert"));
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		assertEquals(6, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(whiteHand);
		assertEquals(5, scn.GetResistance(aragorn));

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(6, scn.GetResistance(aragorn));
		// Gandalf's too: Saruman's sentence is off for every companion.
		assertEquals(6, scn.GetResistance(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithGandalfAndTwoElvenAlliesEverySarumanCanBeWoundedByArcheryAndAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var aragorn = scn.GetFreepsCard("aragorn");
		var keeper = scn.GetShadowCard("keeper");
		var servant = scn.GetShadowCard("servant");
		var traitor = scn.GetShadowCard("traitor");
		var master = scn.GetShadowCard("master");
		var keeperPC = scn.GetShadowCard("keeperPC");
		var fellVoice = scn.GetShadowCard("fellVoice");
		var rabble = scn.GetShadowCard("rabble");
		var instigator = scn.GetShadowCard("instigator");
		var mind = scn.GetShadowCard("mind");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(council);
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("legolas"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("golradir"), scn.GetFreepsCard("saelbeth"), scn.GetFreepsCard("albert"));

		scn.StartGame();
		// The minions go down once the fellowship has moved, so that nothing of theirs answers the move.
		scn.FreepsPassCurrentPhaseAction();
		scn.MoveMinionsToTable(keeper, servant, traitor, master, keeperPC, fellVoice, rabble, instigator, mind, troop, savage);
		scn.SkipToArcheryWounds();

		// The Shadow player may put Legolas's one archery wound on any minion, the six who "may not take
		// wounds during the archery phase" among them.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.ShadowHasCardChoiceAvailable(keeper, servant, traitor, master, keeperPC, fellVoice));
		assertTrue(scn.ShadowHasCardChoiceAvailable(rabble, instigator, mind, troop, savage));
		assertEquals(0, scn.GetWoundsOn(fellVoice));
		scn.ShadowChooseCard(fellVoice);
		assertEquals(1, scn.GetWoundsOn(fellVoice));

		// Only the first sentence is off.  The Keeper of Isengard's second, "Uruk-hai are fierce", still
		// applies to the Uruk Savage.
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		// All nine Sarumans may be assigned to a skirmish: by the Free Peoples player, who is offered every
		// minion on the table...
		scn.PassAssignmentActions();
		assertEquals(11, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(keeper));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(servant));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(traitor));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(master));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(keeperPC));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(fellVoice));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(rabble));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(instigator));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(mind));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		scn.FreepsAssignToMinions(aragorn, keeper);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, keeper));

		// ...and, of the ten minions left over, by the Shadow player: the eight Sarumans not yet assigned,
		// the Troop and the Savage.
		assertEquals(10, scn.ShadowGetShadowAssignmentTargetCount());
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(keeper));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(servant));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(traitor));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(master));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(keeperPC));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(fellVoice));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(rabble));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(instigator));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(mind));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(savage));
		scn.ShadowAssignToMinions(aragorn, rabble);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, rabble));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithGandalfAndOnlyOneElvenAllyNoSarumanCanBeWoundedByArcheryOrAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var aragorn = scn.GetFreepsCard("aragorn");
		var keeper = scn.GetShadowCard("keeper");
		var servant = scn.GetShadowCard("servant");
		var traitor = scn.GetShadowCard("traitor");
		var master = scn.GetShadowCard("master");
		var keeperPC = scn.GetShadowCard("keeperPC");
		var fellVoice = scn.GetShadowCard("fellVoice");
		var rabble = scn.GetShadowCard("rabble");
		var instigator = scn.GetShadowCard("instigator");
		var mind = scn.GetShadowCard("mind");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(council);
		// The board of the test above without Saelbeth: Gandalf, one [elven] ally, an [elven] companion and
		// a [gandalf] ally.
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("legolas"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("golradir"), scn.GetFreepsCard("albert"));

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.MoveMinionsToTable(keeper, servant, traitor, master, keeperPC, fellVoice, rabble, instigator, mind, troop, savage);
		scn.SkipToArcheryWounds();

		// Legolas's archery wound cannot be put on the six who may not take wounds during the archery phase.
		// The three whose first sentence only keeps them out of skirmishes can be given it.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(keeper, servant, traitor, master, keeperPC, fellVoice));
		assertTrue(scn.ShadowHasCardChoiceAvailable(rabble, instigator, mind, troop, savage));
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));

		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		// Only the Goblin Patrol Troop and the Uruk Savage can be assigned, by either player.  The Free
		// Peoples player is offered those two and no Saruman.
		scn.PassAssignmentActions();
		assertEquals(2, scn.FreepsGetShadowAssignmentTargetCount());
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(keeper));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(servant));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(traitor));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(master));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(keeperPC));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(fellVoice));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(rabble));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(instigator));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(mind));
		scn.FreepsAssignToMinions(aragorn, troop);

		// The Shadow player is left the Savage and nothing else.
		assertEquals(1, scn.ShadowGetShadowAssignmentTargetCount());
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(savage));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(troop));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(keeper));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(servant));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(traitor));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(master));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(keeperPC));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(fellVoice));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(rabble));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(instigator));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(mind));
		scn.ShadowAssignToMinions(aragorn, savage);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertFalse(scn.IsCharAssigned(keeper));
		assertFalse(scn.IsCharAssigned(rabble));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void CurunirsFirstSentenceHealsAnUrukHaiWithOneElvenAllyAndNotOnceThereAreTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var curunir = scn.GetShadowCard("curunir");
		var tracker1 = scn.GetShadowCard("tracker1");
		var tracker2 = scn.GetShadowCard("tracker2");
		var tracker3 = scn.GetShadowCard("tracker3");
		scn.MoveCardsToSupportArea(council);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(scn.GetFreepsCard("golradir"));

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.MoveMinionsToTable(curunir, tracker1, tracker2, tracker3);
		// The third Tracking Uruk is the only wounded [uruk-hai] minion.
		scn.AddWoundsToChar(tracker3, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { gandalf, tracker1 }, new PhysicalCardImpl[] { aragorn, tracker2 });
		scn.ShadowDeclineAssignments();
		assertEquals(2, scn.GetWoundsOn(tracker3));

		// Gandalf and one [elven] ally.  A Tracking Uruk (14 + 1 for hunter) beats Gandalf (6 + 1 for the
		// Hobbit and + 1 for the Man in the fellowship), and Curunir heals the wounded one.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		// Curunir's trigger and the skirmish's wound come at once, and the game asks which is first.
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(tracker3));

		// A second [elven] ally arrives.  The other Tracking Uruk beats Aragorn (8), and nothing is healed.
		scn.MoveAlliesToTable(scn.GetFreepsCard("saelbeth"));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(tracker3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	@Ignore("F-0037: with Gandalf and 2 [elven] allies spotted, the first ability of Gandalf, Leader of Dwarves (30_25) is switched off as well; the text says Saruman's")
	public void AGandalfOfTheHobbitSetsKeepsHisFirstSentence() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var hobbitGandalf = scn.GetFreepsCard("hobbitGandalf");
		scn.MoveCardsToSupportArea(council);
		// Gandalf and two [elven] allies: Saruman's first sentence does not apply.  This Gandalf is the one
		// spotted, and he is not Saruman.
		scn.MoveCompanionsToTable(hobbitGandalf);
		scn.MoveAlliesToTable(scn.GetFreepsCard("golradir"), scn.GetFreepsCard("saelbeth"));

		scn.StartGame();
		// With no minion in play the turn goes straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hobbitGandalf));

		// The fellowship stays and its owner reconciles, which is what this Gandalf's first sentence answers.
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hobbitGandalf));
	}

	@Test
	public void RegroupActionExertsTwoElvenAlliesToHealGandalfOrACompanionWithTheGandalfSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var golradir = scn.GetFreepsCard("golradir");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var celeborn = scn.GetFreepsCard("celeborn");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCardsToSupportArea(council);
		scn.MoveCompanionsToTable(gandalf, aragorn, legolas);
		scn.MoveAlliesToTable(golradir, saelbeth, celeborn, albert);
		// Wounded: Gandalf (whose signet is Frodo's), Aragorn (the Gandalf signet), Legolas and Frodo (the
		// Frodo signet), and Albert Dreary, an ally.
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(albert, 1);

		scn.StartGame();

		// Not a fellowship action.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(council));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(council));
		assertEquals(0, scn.GetWoundsOn(golradir));
		assertEquals(0, scn.GetWoundsOn(saelbeth));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		scn.FreepsUseCardAction(council);
		// Any two of the three [elven] allies, whose homes are sites 3, 3 and 6; not the [elven] companion
		// and not the [gandalf] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(golradir, saelbeth, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, albert));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(golradir, celeborn);
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(saelbeth));
		assertEquals(1, scn.GetWoundsOn(albert));

		// Gandalf, or a companion with the Gandalf signet; not a companion with another signet, and not an
		// ally, [gandalf] or just exerted.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, frodo, albert, golradir, celeborn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gandalf));

		// A second use, for Gandalf.
		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.FreepsActionAvailable(council));
		scn.FreepsUseCardAction(council);
		scn.FreepsChooseCards(saelbeth, celeborn);
		assertEquals(1, scn.GetWoundsOn(saelbeth));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn));
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhenOnlyOneElvenAllyCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var golradir = scn.GetFreepsCard("golradir");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCardsToSupportArea(council);
		scn.MoveCompanionsToTable(gandalf, legolas);
		scn.MoveAlliesToTable(golradir, saelbeth, albert);
		scn.AddWoundsToChar(gandalf, 2);
		// Golradir has vitality 3 and 2 wounds: he cannot exert.  Saelbeth could exert twice, but it takes
		// two allies.  The [elven] companion and the [gandalf] ally could each exert, and are no help.
		scn.AddWoundsToChar(golradir, 2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(saelbeth));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsActionAvailable(council));
	}

	@Test
	public void RegroupActionCanBeUsedTwiceInARegroupPhaseAndAgainInTheNextRegroupPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var council = scn.GetFreepsCard("council");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var golradir = scn.GetFreepsCard("golradir");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCardsToSupportArea(council);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(golradir, saelbeth, celeborn);
		scn.AddWoundsToChar(gandalf, 3);
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(council));
		scn.FreepsUseCardAction(council);
		scn.FreepsChooseCards(golradir, saelbeth);
		scn.FreepsChooseCard(gandalf);
		assertEquals(2, scn.GetWoundsOn(gandalf));

		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.FreepsActionAvailable(council));
		scn.FreepsUseCardAction(council);
		scn.FreepsChooseCards(golradir, celeborn);
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Twice is the limit.  Saelbeth and Celeborn, with 1 wound each on vitality 3, could pay a third
		// time, and Gandalf and Aragorn are still wounded.
		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(golradir));
		assertEquals(1, scn.GetWoundsOn(saelbeth));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsActionAvailable(council));

		// The fellowship moves again.  With no minion in play the second regroup phase of the turn follows,
		// and the limit is that phase's own.
		scn.PassRegroupActions();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(council));
		scn.FreepsUseCardAction(council);
		// Saelbeth and Celeborn are the only two who can exert.
		assertEquals(2, scn.GetWoundsOn(saelbeth));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
