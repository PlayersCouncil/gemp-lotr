package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treasure", "103_119");    // Treasured Pipe
					put("treasure2", "103_119");   // a second copy: the card is not unique
					put("treasure3", "103_119");   // a third copy, hindered where a pipe that cannot be spotted is wanted
					put("aragornspipe", "1_91");   // Aragorn's Pipe: a [gondor] pipe, on a [gondor] companion; its own fellowship ability is never used
					put("toby", "1_305");          // Old Toby: pipeweed possession, support area; when played, may draw a card
					put("toby2", "1_305");
					put("longbottom", "1_300");    // Longbottom Leaf: pipeweed possession, support area; a when-played trigger only
					put("halfling", "17_106");     // Halfling Leaf: pipeweed condition, support area; a skirmish ability never used here
					put("flotsam", "103_106");     // Flotsam: pipeweed possession borne by a character who bears a pipe; a when-played trigger only
					put("sword", "1_299");         // Hobbit Sword: a [shire] possession that is no pipeweed; strength +2
					put("sam", "1_311");           // Sam, Son of Hamfast: Ring-bound Hobbit companion, strength 3
					put("merry", "1_302");         // Merry, Friend to Sam: unbound Hobbit companion, strength 3; a skirmish ability never used here
					put("pippin", "1_306");        // Pippin, Friend to Frodo: unbound Hobbit companion; his text is about tales
					// Pippin, Trollslayer: an unbound Hobbit companion who is a [gondor] card, cost 2; to play, spot a
					// [gondor] companion. A skirmish ability never used here. Never in play beside the other Pippin.
					put("trollslayer", "103_31");
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: unbound Man, [gondor], strength 8; a maneuver ability never used here
					put("bounder", "1_286");       // Bounder: a [shire] Hobbit ally; a skirmish ability never used here

					put("runner1", "1_178");       // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreasuredPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Treasured Pipe
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: To play, spot a pipe or an unbound Hobbit. Bearer must be a companion.
		 * 	Fellowship: Discard 2 pipeweeds and spot X pipes to make X companions strength +1
		 * 	until the regroup phase.  Hinder this pipe.
		 */

		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");

		assertEquals("Treasured Pipe", treasure.getBlueprint().getTitle());
		assertNull(treasure.getBlueprint().getSubtitle());
		assertFalse(treasure.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, treasure.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, treasure.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, treasure.getBlueprint().getCardType());
		assertTrue(treasure.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, treasure.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnUnboundHobbitIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var hinderedPipe = scn.GetFreepsCard("treasure2");
		var toby = scn.GetFreepsCard("toby");
		var sam = scn.GetFreepsCard("sam");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");

		// Nothing here is a pipe that can be spotted or an unbound Hobbit: Frodo and Sam are Ring-bound Hobbits,
		// the Bounder is a Hobbit and an ally, Aragorn is unbound and no Hobbit, Old Toby is a pipeweed and no
		// pipe, and the pipe Sam bears is hindered.
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(toby);
		scn.AttachCardsTo(sam, hinderedPipe);
		scn.HinderCard(hinderedPipe);
		scn.MoveCardsToHand(treasure, trollslayer);

		scn.StartGame();

		assertTrue(scn.IsHindered(hinderedPipe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(treasure));

		// The unbound Hobbit played is Pippin, Trollslayer, a [gondor] card: the text asks for the race and not
		// for the [shire] culture. Aragorn is the [gondor] companion Pippin needs to be played.
		assertEquals(Culture.GONDOR, trollslayer.getBlueprint().getCulture());
		scn.FreepsPlayCard(trollslayer);

		assertInZone(Zone.FREE_CHARACTERS, trollslayer);
		assertTrue(scn.IsRace(trollslayer, Race.HOBBIT));
		assertTrue(scn.FreepsPlayAvailable(treasure));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnotherPipeIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");

		// No unbound Hobbit: Frodo and Sam are Ring-bound
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(treasure, aragornspipe);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(treasure));

		// A pipe of another culture; Aragorn is the only companion who may bear it
		scn.FreepsPlayCard(aragornspipe);
		assertAttachedTo(aragornspipe, aragorn);

		assertTrue(scn.FreepsPlayAvailable(treasure));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedOnACompanionOfAnyKindAndNotOnAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");

		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(treasure);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(treasure));
		scn.FreepsPlayCard(treasure);

		// The Ring-bearer, a Man of another culture and an unbound Hobbit
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, merry));
		// A Hobbit who is an ally
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(treasure, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsTwoPipeweedsAndSpotsPipesToStrengthenThatManyCompanionsThenHindersItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var treasure2 = scn.GetFreepsCard("treasure2");
		var hinderedPipe = scn.GetFreepsCard("treasure3");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var toby = scn.GetFreepsCard("toby");
		var hinderedToby = scn.GetFreepsCard("toby2");
		var longbottom = scn.GetFreepsCard("longbottom");
		var halfling = scn.GetFreepsCard("halfling");
		var flotsam = scn.GetFreepsCard("flotsam");
		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");

		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		// Three pipes that can be spotted, two of them [shire], and a fourth that is hindered
		scn.AttachCardsTo(merry, treasure);
		scn.AttachCardsTo(frodo, treasure2);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.AttachCardsTo(sam, hinderedPipe, sword);
		// Four pipeweeds that can be spotted (two possessions and a condition in the support area, a possession
		// Aragorn bears) and a fifth that is hindered
		scn.MoveCardsToSupportArea(toby, longbottom, halfling, hinderedToby);
		scn.AttachCardsTo(aragorn, flotsam);
		scn.HinderCard(hinderedPipe, hinderedToby, pippin);

		scn.StartGame();

		assertTrue(scn.IsHindered(hinderedPipe));
		assertTrue(scn.IsHindered(hinderedToby));
		assertTrue(scn.IsHindered(pippin));
		assertTrue(scn.FreepsActionAvailable(treasure));
		assertTrue(scn.FreepsActionAvailable(treasure2));

		int frodoStrength = scn.GetStrength(frodo);
		int samStrength = scn.GetStrength(sam);
		int merryStrength = scn.GetStrength(merry);
		int aragornStrength = scn.GetStrength(aragorn);
		int bounderStrength = scn.GetStrength(bounder);

		scn.FreepsUseCardAction(treasure, "Use");

		// Exactly 2 of the pipeweeds that can be spotted, wherever they lie
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, longbottom, halfling, flotsam));
		// A hindered pipeweed, the pipes, and a [shire] possession that is no pipeweed
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hinderedToby, aragornspipe, treasure, treasure2, sword));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(toby, flotsam);

		assertInDiscard(toby, flotsam);
		assertInZone(Zone.SUPPORT, longbottom, halfling, hinderedToby);

		// X: up to the 3 pipes that can be spotted. Not 4 (the hindered pipe, or the pipeweeds there were),
		// not 2 (the [shire] pipes, or the pipeweeds left).
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("3");

		// Exactly 3 companions: the Ring-bearer, a Ring-bound Hobbit, an unbound Hobbit and a Man are offered
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, merry, aragorn));
		// A hindered companion and an ally
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, bounder));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(frodo, merry, aragorn);

		assertEquals(frodoStrength + 1, scn.GetStrength(frodo));
		assertEquals(merryStrength + 1, scn.GetStrength(merry));
		assertEquals(aragornStrength + 1, scn.GetStrength(aragorn));
		assertEquals(samStrength, scn.GetStrength(sam));
		assertEquals(bounderStrength, scn.GetStrength(bounder));

		// This pipe is hindered, and nothing else is: not the other copy, the other pipe, its bearer or the
		// pipeweeds left.
		assertTrue(scn.IsHindered(treasure));
		assertFalse(scn.IsHindered(treasure2));
		assertFalse(scn.IsHindered(aragornspipe));
		assertFalse(scn.IsHindered(merry));
		assertFalse(scn.IsHindered(longbottom));
		assertFalse(scn.IsHindered(halfling));

		// Two pipeweeds are left to discard: the hindered pipe offers nothing, the other copy still does
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(treasure));
		assertTrue(scn.FreepsActionAvailable(treasure2));

		scn.FreepsUseCardAction(treasure2, "Use");

		// The two pipeweeds left are the two it needs
		assertInDiscard(longbottom, halfling);
		assertInZone(Zone.SUPPORT, hinderedToby);

		// The first copy is hindered and is no longer a pipe that can be spotted: 2, not 3
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, merry, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(sam);

		assertEquals(samStrength + 1, scn.GetStrength(sam));
		assertEquals(frodoStrength + 1, scn.GetStrength(frodo));
		assertEquals(merryStrength + 1, scn.GetStrength(merry));
		assertEquals(aragornStrength + 1, scn.GetStrength(aragorn));

		assertTrue(scn.IsHindered(treasure2));
		assertFalse(scn.IsHindered(aragornspipe));
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingNoPipesStillDiscardsThePipeweedsAndHindersThePipeAndStrengthensNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var treasure2 = scn.GetFreepsCard("treasure2");
		var toby = scn.GetFreepsCard("toby");
		var longbottom = scn.GetFreepsCard("longbottom");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, treasure);
		scn.AttachCardsTo(frodo, treasure2);
		scn.MoveCardsToSupportArea(toby, longbottom);

		scn.StartGame();

		int frodoStrength = scn.GetStrength(frodo);
		int merryStrength = scn.GetStrength(merry);

		scn.FreepsUseCardAction(treasure, "Use");

		// The only two pipeweeds are discarded before the pipes are spotted
		assertInDiscard(toby, longbottom);

		// A player need not spot what he could: none, one or both pipes
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("0");

		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(merryStrength, scn.GetStrength(merry));
		assertTrue(scn.IsHindered(treasure));
		assertFalse(scn.IsHindered(treasure2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithOnePipeweedInPlayAndOfferedOnceASecondIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var longbottom = scn.GetFreepsCard("longbottom");
		var toby = scn.GetFreepsCard("toby");
		var hinderedToby = scn.GetFreepsCard("toby2");
		var frodo = scn.GetRingBearer();

		// One pipeweed that can be discarded. Beside it: a hindered pipeweed, a pipeweed in hand, and the pipe
		// itself, which is no pipeweed.
		scn.AttachCardsTo(frodo, treasure);
		scn.MoveCardsToSupportArea(longbottom, hinderedToby);
		scn.HinderCard(hinderedToby);
		scn.MoveCardsToHand(toby);

		scn.StartGame();

		assertTrue(scn.IsHindered(hinderedToby));
		assertFalse(scn.FreepsActionAvailable(treasure));

		scn.FreepsPlayCard(toby);
		// Old Toby's own "you may draw a card"
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.FreepsActionAvailable(treasure));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StrengthLastsThroughTheSkirmishesAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var treasure2 = scn.GetFreepsCard("treasure2");
		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		var longbottom = scn.GetFreepsCard("longbottom");
		var halfling = scn.GetFreepsCard("halfling");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, treasure);
		scn.AttachCardsTo(aragorn, treasure2);
		scn.MoveCardsToSupportArea(toby, toby2, longbottom, halfling);

		scn.StartGame();

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));

		// "Use": Frodo bears no pipe here, so the pipe's transfer to him is on offer beside its ability
		scn.FreepsUseCardAction(treasure, "Use");
		scn.FreepsChooseCards(toby, longbottom);
		scn.FreepsChoose("2");
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, aragorn));
		scn.FreepsChooseCards(merry, aragorn);

		assertEquals(4, scn.GetStrength(merry));
		assertEquals(9, scn.GetStrength(aragorn));

		// The maneuver phase: the other copy is not hindered and two pipeweeds are in play, and it offers nothing
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.IsHindered(treasure2));
		assertInZone(Zone.SUPPORT, toby2, halfling);
		assertFalse(scn.FreepsActionAvailable(treasure2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner1}, new PhysicalCardImpl[]{aragorn, runner2});

		// Merry 3 + 1 against 5: he loses and takes a wound
		scn.FreepsResolveSkirmish(merry);
		assertEquals(4, scn.GetStrength(merry));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(merry));

		// The second skirmish: both are still strength +1
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(merry));
		scn.PassCurrentPhaseActions();

		// The regroup phase has started
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(merry));
	}
}
