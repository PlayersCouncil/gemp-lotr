package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dagger", "17_44");
					put("grima", "13_92");      // Gríma, Footman of Saruman: [men] Man, strength 4; lurker; a trigger on a Free Peoples character killed only
					put("hillsman", "17_50");   // Stampeding Hillsman: [men] Man of another title, strength 7; a regroup ability only
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] minion who is no Man (a Wizard)
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					put("gollum", "5_25");      // Gollum, Stinker: a [gollum] Shadow card, which shows no Free Peoples culture

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a second [gondor] companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] companion, no requirement to play
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion, no requirement to play
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession a companion bears; minion archery total -1

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5
					put("drums", "1_168");      // Drums in the Deep: Shadow skirmish event, make a [moria] Orc strength +2
					put("swordarm", "1_116");   // Swordarm of the White Tower: Free Peoples skirmish event, make a [gondor] companion strength +2
					// Flanking Attack: [raider] event, cost 0. "Maneuver or Skirmish: Exert a Southron to add (3)."
					put("flanking", "4_234");
					put("lancers", "4_217");    // Desert Lancers: [raider] Southron, strength 11, vitality 3; a regroup ability only
					put("haradrim", "5_70");    // Army of Haradrim: Southron, strength 14; "Skirmish: Remove (3) to play a [raider] mount from your discard pile"
					put("mumak", "5_78");       // War Mumak: [raider] mount possession, played on the Army in the skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimasDaggerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Gríma's Dagger
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be a [men] Man.
		 * If bearer is Gríma, he is strength +1 for each Free Peoples culture you can spot over 2.
		 * <b>Response:</b> If a skirmish event is played, discard this possession to cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("dagger");

		assertEquals("Gríma's Dagger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAMenManOfAnyTitleAndOnNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetShadowCard("dagger");
		var grima = scn.GetShadowCard("grima");
		var hillsman = scn.GetShadowCard("hillsman");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(dagger);
		assertInHand(dagger);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, hillsman, saruman, brigand);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(dagger));
		scn.ShadowPlayCard(dagger);

		assertTrue(scn.ShadowHasCardChoiceAvailable(grima, hillsman));
		// A [men] minion who is no Man, a Man of another culture, and the Free Peoples player's companion.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, brigand, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(grima);

		assertAttachedTo(dagger, grima);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileGrimaBearsItHeIsStrengthPlusOneForEachFreePeoplesCultureOverTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetShadowCard("dagger");
		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(gimli, gandalf);
		assertInHand(gimli, gandalf);

		scn.StartGame();
		// Laid early, as he will stand in play later in the turn (Q-0228).
		scn.MoveMinionsToTable(grima);
		scn.AttachCardsTo(grima, dagger);
		scn.FreepsRefreshDecision();
		assertAttachedTo(dagger, grima);

		// [shire] and [gondor]: two cultures, the second [gondor] companion counting for nothing. 4 + 1 for the Dagger.
		assertEquals(5, scn.GetStrength(grima));

		scn.FreepsPlayCard(gimli);
		// [dwarven] is the third culture: one over 2.
		assertEquals(6, scn.GetStrength(grima));

		scn.FreepsPlayCard(gandalf);
		// [gandalf] is the fourth: two over 2.
		assertEquals(7, scn.GetStrength(grima));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACultureShownByAPossessionCountsAndAGollumShadowCardDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetShadowCard("dagger");
		var grima = scn.GetShadowCard("grima");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, gollum);
		scn.AttachCardsTo(grima, dagger);
		scn.FreepsRefreshDecision();

		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(dagger, grima);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		// [shire], [gondor], and [elven] by the Cloak: one over 2 (4 + 1 for the Dagger + 1).
		// Counting the [gollum] minion as a Free Peoples culture would read 7; leaving out the Cloak, 5.
		assertEquals(6, scn.GetStrength(grima));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByAnotherMenManItGivesNothingAndGrimaBesideItGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetShadowCard("dagger");
		var grima = scn.GetShadowCard("grima");
		var hillsman = scn.GetShadowCard("hillsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, hillsman);
		scn.AttachCardsTo(hillsman, dagger);
		scn.FreepsRefreshDecision();

		assertAttachedTo(dagger, hillsman);
		// Four Free Peoples cultures, two over 2: the bearer is no Gríma, so only the printed +1 (7 + 1),
		// and the Gríma who does not bear it stays at 4.
		assertEquals(8, scn.GetStrength(hillsman));
		assertEquals(4, scn.GetStrength(grima));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenTheFreePeoplesPlayerPlaysASkirmishEventItIsDiscardedToCancelThatEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetShadowCard("dagger");
		var grima = scn.GetShadowCard("grima");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var swordarm = scn.GetFreepsCard("swordarm");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(swordarm);
		assertInHand(swordarm);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, runner);
		scn.AttachCardsTo(grima, dagger);
		assertAttachedTo(dagger, grima);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		// Gríma is in no skirmish: the text asks for none.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertAttachedTo(dagger, grima);
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(swordarm);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dagger));
		scn.ShadowAcceptOptionalTrigger();

		// The Dagger is discarded, and the event cancelled: no +2, and it goes to its owner's discard pile.
		assertInDiscard(dagger, swordarm);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ItsOwnersShadowSkirmishEventIsAnsweredAndCancelledToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetShadowCard("dagger");
		var grima = scn.GetShadowCard("grima");
		var runner = scn.GetShadowCard("runner");
		var drums = scn.GetShadowCard("drums");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(drums);
		assertInHand(drums);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, runner);
		scn.AttachCardsTo(grima, dagger);
		assertAttachedTo(dagger, grima);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertAttachedTo(dagger, grima);
		assertEquals(5, scn.GetStrength(runner));
		scn.ShadowPlayCard(drums);

		// "A skirmish event" names no side.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dagger));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(dagger, drums);
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AShadowCardThatIsNoEventPlayedInASkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetShadowCard("dagger");
		var grima = scn.GetShadowCard("grima");
		var haradrim = scn.GetShadowCard("haradrim");
		var mumak = scn.GetShadowCard("mumak");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(mumak);
		assertInDiscard(mumak);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, haradrim);
		scn.AttachCardsTo(grima, dagger);
		assertAttachedTo(dagger, grima);
		scn.SkipToAssignments();
		scn.SetTwilight(6);
		scn.FreepsAssignToMinions(aragorn, haradrim);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertAttachedTo(dagger, grima);
		assertTrue(scn.ShadowActionAvailable(haradrim));
		scn.ShadowUseCardAction(haradrim);

		// A possession played in the skirmish is no event.
		assertAttachedTo(mumak, haradrim);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dagger));
		assertAttachedTo(dagger, grima);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnEventOfTwoPhasesPlayedInTheManeuverPhaseIsNoSkirmishEventAndIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetShadowCard("dagger");
		var grima = scn.GetShadowCard("grima");
		var flanking = scn.GetShadowCard("flanking");
		var lancers = scn.GetShadowCard("lancers");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(flanking);
		assertInHand(flanking);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, lancers);
		scn.AttachCardsTo(grima, dagger);
		assertAttachedTo(dagger, grima);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertAttachedTo(dagger, grima);
		assertEquals(0, scn.GetWoundsOn(lancers));
		assertTrue(scn.ShadowPlayAvailable(flanking));
		scn.ShadowPlayCard(flanking);

		// "Maneuver or Skirmish", played in the maneuver phase, is a maneuver event (F-0292).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(dagger));
		assertEquals(1, scn.GetWoundsOn(lancers));
		assertInDiscard(flanking);
		assertAttachedTo(dagger, grima);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
