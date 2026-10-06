package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("phial", "103_1");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit. "Response: If Frodo dies, make Sam the Ring-bearer"
					put("pippin", "1_306");     // Pippin, Friend to Frodo: an unbound Hobbit, cost 1
					// Bilbo, Melancholy Hobbit: a Ring-bound Hobbit who is never the Ring-bearer; an offer at the start of
					// a skirmish involving him and nothing else
					put("bilbo", "12_119");
					put("smeagol", "6_45");     // Smeagol, Poor Creature: Ring-bound, [gollum], no race; a skirmish ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: "Maneuver: Exert Aragorn to make him defender +1"
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength modifier and nothing else
					put("bounder", "1_286");    // Bounder: Hobbit ally, cost 1; a skirmish ability
					put("sword", "1_299");      // Hobbit Sword: bearer must be a Hobbit, cost 1, no text
					put("toby", "1_305");       // Old Toby: a Free Peoples possession that sits in the support area
					put("stone", "13_36");      // The Palantir of Orthanc, Recovered Seeing Stone: a Free Peoples artifact borne by Gandalf

					put("armory1", "1_157");    // Uruk-hai Armory: [isengard] condition, support area; does nothing without an Uruk-hai
					put("armory2", "1_157");
					put("pinned", "1_192");     // Pinned Down: [moria] condition, support area; does nothing without a [moria] archer
					put("breath1", "1_207");    // Black Breath: [wraith] condition, support area; moves onto a character in a skirmish
					put("breath2", "1_207");
					put("palantir", "3_67");    // The Palantir of Orthanc: [isengard] artifact, support area; a Shadow-phase ability
					put("troop", "1_177");      // Goblin Patrol Troop: no text
					put("marksman", "1_176");   // Goblin Marksman: Archer and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PhialofGaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: V3
		 * Name: Phial of Galadriel, Bane of Darkness
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Phial
		 * Vitality: +1
		 * Game Text: Bearer must be a Ring-bound Hobbit. Discard this if you can spot more than 4 companions.
		 * 	Maneuver: Add 2 burdens to hinder all Shadow support cards with a title you spot.
		 * 	Response: If The One Ring is transferred, play this on the new Ring-bearer.  You may use this ability from your discard pile.
		 */

		var scn = GetScenario();
		var card = scn.GetFreepsCard("phial");

		assertEquals("Phial of Galadriel", card.getBlueprint().getTitle());
		assertEquals("Bane of Darkness", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PHIAL));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedForOneOnARingBoundHobbitWhoGainsAVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, pippin, smeagol);
		scn.MoveCardsToHand(phial);

		scn.StartGame();
		// Smeagol is Ring-bound and no Hobbit. Made [shire] as well, he would be let through by a card that took
		// "Hobbit" to mean the culture.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, smeagol, null, Culture.SHIRE));

		assertEquals(0, scn.GetTwilight());
		assertEquals(4, scn.GetVitality(sam));
		assertTrue(scn.FreepsPlayAvailable(phial));

		scn.FreepsPlayCard(phial);

		// Frodo, the Ring-bearer, and Sam are the Ring-bound Hobbits. Pippin is a Hobbit and unbound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(sam);

		assertAttachedTo(phial, sam);
		assertEquals(1, scn.GetTwilight());
		assertEquals(5, scn.GetVitality(sam));
		assertEquals(4, scn.GetVitality(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardedWhenAFifthCompanionIsPlayedButNotWhenAnAllyJoinsFourCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var pippin = scn.GetFreepsCard("pippin");
		// Frodo and three more: 4 companions
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.AttachCardsTo(frodo, phial);
		scn.MoveCardsToHand(bounder, pippin);

		scn.StartGame();

		assertAttachedTo(phial, frodo);
		// Vitality 4 + 1 for the Phial
		assertEquals(5, scn.GetVitality(frodo));

		// An ally is a fifth character and not a fifth companion.
		scn.FreepsPlayCard(bounder);

		assertInPlay(bounder);
		assertAttachedTo(phial, frodo);
		assertEquals(5, scn.GetVitality(frodo));

		scn.FreepsPlayCard(pippin);

		// 5 companions is more than 4.
		assertInPlay(pippin);
		assertInZone(Zone.DISCARD, phial);
		assertEquals(4, scn.GetVitality(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityAddsTwoBurdensAndHindersEveryShadowSupportCardOfTheTitleSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var toby = scn.GetFreepsCard("toby");
		var armory1 = scn.GetShadowCard("armory1");
		var armory2 = scn.GetShadowCard("armory2");
		var pinned = scn.GetShadowCard("pinned");
		var breath1 = scn.GetShadowCard("breath1");
		var palantir = scn.GetShadowCard("palantir");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(frodo, phial);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToSupportArea(armory1, armory2, pinned, breath1, palantir);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(phial));

		scn.FreepsUseCardAction(phial);

		assertEquals(2, scn.GetBurdens());
		// Conditions of three cultures and an artifact, all in the Shadow player's support area. Old Toby is in a
		// support area and is no Shadow card; the Troop is a Shadow card and is in no support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory1, armory2, pinned, breath1, palantir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(toby, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(armory1);

		// Both copies of the title spotted, and nothing of any other title.
		assertTrue(scn.IsHindered(armory1));
		assertTrue(scn.IsHindered(armory2));
		assertFalse(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(breath1));
		assertFalse(scn.IsHindered(palantir));
		assertFalse(scn.IsHindered(toby));
		assertFalse(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(phial));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// It may be used again in the same phase, for 2 more burdens.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(phial));
		scn.FreepsUseCardAction(phial);

		assertEquals(4, scn.GetBurdens());
		scn.FreepsChooseCard(palantir);

		assertTrue(scn.IsHindered(palantir));
		assertFalse(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(breath1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityHindersOnlyShadowSupportCardsOfTheTitleSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var stone = scn.GetFreepsCard("stone");
		var breath1 = scn.GetShadowCard("breath1");
		var breath2 = scn.GetShadowCard("breath2");
		var palantir = scn.GetShadowCard("palantir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(frodo, phial);
		scn.MoveCardsToSupportArea(breath1, palantir);
		// A Black Breath that has left the support area for a character, as its own ability moves it.
		scn.AttachCardsTo(aragorn, breath2);

		scn.StartGame();
		// The Free Peoples player's own artifact of the same title as the Shadow player's Palantir. Put down after
		// the start of the fellowship phase, when it would have made its offer.
		scn.AttachCardsTo(gandalf, stone);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		scn.FreepsUseCardAction(phial);
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsHasCardChoiceAvailable(breath1, palantir));
		scn.FreepsChooseCard(breath1);

		// The Black Breath on Aragorn is a Shadow card of that title and is not a support card.
		assertTrue(scn.IsHindered(breath1));
		assertFalse(scn.IsHindered(breath2));
		assertFalse(scn.IsHindered(palantir));
		assertFalse(scn.IsHindered(stone));

		scn.ShadowPass();
		scn.FreepsUseCardAction(phial);
		assertEquals(4, scn.GetBurdens());

		// One Shadow support card is left unhindered, so its title is the one spotted.
		// Gandalf's Palantir has that title and is not a Shadow card.
		assertTrue(scn.IsHindered(palantir));
		assertFalse(scn.IsHindered(stone));
		assertFalse(scn.IsHindered(breath2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityWithNoShadowSupportCardStillAddsItsTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var toby = scn.GetFreepsCard("toby");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(frodo, phial);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetBurdens());
		// An ability with a cost is offered whenever the cost can be paid.
		assertTrue(scn.FreepsActionAvailable(phial));

		scn.FreepsUseCardAction(phial);

		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.IsHindered(toby));
		assertFalse(scn.IsHindered(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsOfferedInTheManeuverPhaseOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var armory1 = scn.GetShadowCard("armory1");
		var troop = scn.GetShadowCard("troop");
		scn.AttachCardsTo(frodo, phial);
		scn.MoveCardsToSupportArea(armory1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(phial));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(phial));

		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.FreepsActionAvailable(phial));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(armory1));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileTheRingBearerIsHinderedAndCanBeGivenNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory1 = scn.GetShadowCard("armory1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, phial);
		scn.MoveCardsToSupportArea(armory1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(phial));

		// Burdens cannot be added while the Ring-bearer is hindered, so the cost cannot be paid.
		scn.HinderCard(frodo);
		// Aragorn's own maneuver ability, to have the Free Peoples player asked afresh.
		scn.FreepsUseCardAction(aragorn);
		scn.ShadowPass();

		assertTrue(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(phial));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(armory1));
	}

	@Test
	public void WhenFrodoDiesBearingItTheResponsePlaysItFromTheDiscardPileOnSamTheNewRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var marksman = scn.GetShadowCard("marksman");
		// Bilbo is a Ring-bound Hobbit the Phial could be played on, and not the one its response names.
		scn.MoveCompanionsToTable(sam, bilbo);
		scn.AttachCardsTo(frodo, phial);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		// Vitality 4 + 1 for the Phial: four wounds leave Frodo one from death.
		scn.AddWoundsToChar(frodo, 4);
		scn.SkipToArcheryWounds();

		assertAttachedTo(phial, frodo);
		// 3 companions + 2 for the shadow number of site 2
		assertEquals(5, scn.GetTwilight());

		// The Marksman's one archery wound
		scn.FreepsChooseCard(frodo);

		assertInZone(Zone.DEAD, frodo);
		// The Phial went to the discard pile with its bearer.
		assertInZone(Zone.DISCARD, phial);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(phial));

		scn.FreepsChooseOptionalTrigger(sam);

		assertEquals(sam, scn.GetRingBearer());
		assertAttachedTo(ring, sam);
		assertInZone(Zone.DISCARD, phial);
		assertEquals(4, scn.GetVitality(sam));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(phial));

		scn.FreepsChooseOptionalTrigger(phial);

		// On Sam without a choice: Bilbo was not a candidate.
		assertAttachedTo(phial, sam);
		assertNotAttachedTo(phial, bilbo);
		// 5 + 1 to play the Phial
		assertEquals(6, scn.GetTwilight());
		assertEquals(5, scn.GetVitality(sam));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void WhenFrodoDiesWithThePhialOnBilboTheResponseMovesItToSamTheNewRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var pippin = scn.GetFreepsCard("pippin");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(sam, bilbo, pippin);
		scn.AttachCardsTo(bilbo, phial);

		scn.StartGame();
		// With Frodo dead, Sam the Ring-bearer and Bilbo bearing the Phial, no card is a Ring-bound Hobbit besides, so
		// Pippin is made one: a Hobbit the Phial could go to, and not the one its response names.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, pippin, null, Keyword.RING_BOUND));
		scn.MoveMinionsToTable(marksman);
		// Vitality 4: three wounds leave Frodo one from death.
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		assertTrue(scn.HasKeyword(pippin, Keyword.RING_BOUND));
		// 4 companions + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());

		// The Marksman's one archery wound
		scn.FreepsChooseCard(frodo);

		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(phial));

		scn.FreepsChooseOptionalTrigger(sam);

		assertEquals(sam, scn.GetRingBearer());
		assertAttachedTo(ring, sam);
		assertAttachedTo(phial, bilbo);
		// Vitality 4 + 1 for the Phial
		assertEquals(5, scn.GetVitality(bilbo));
		assertEquals(4, scn.GetVitality(sam));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(phial));

		scn.FreepsChooseOptionalTrigger(phial);

		// On Sam without a choice: Pippin was not a candidate.
		assertAttachedTo(phial, sam);
		assertNotAttachedTo(phial, bilbo);
		assertNotAttachedTo(phial, pippin);
		assertEquals(4, scn.GetVitality(bilbo));
		assertEquals(5, scn.GetVitality(sam));
		// A card moved from one bearer to another by an effect is transferred, and costs nothing.
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void OncePlayedOnTheNewRingBearerTheResponseIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(phial);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		scn.FreepsChooseCard(frodo);
		assertInZone(Zone.DEAD, frodo);
		scn.FreepsChooseOptionalTrigger(sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(phial));
		scn.FreepsChooseOptionalTrigger(phial);

		// It is on Sam and there is nothing more for it to do.
		assertAttachedTo(phial, sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(phial));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheResponseFromTheDiscardPileMayBeDeclinedAndIsNotOfferedToTheOtherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var phial = scn.GetFreepsCard("phial");
		var theirPhial = scn.GetShadowCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(phial, theirPhial);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		// 2 companions + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());

		scn.FreepsChooseCard(frodo);
		assertInZone(Zone.DEAD, frodo);
		scn.FreepsChooseOptionalTrigger(sam);

		assertEquals(sam, scn.GetRingBearer());
		assertAttachedTo(ring, sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(phial));

		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, phial);
		assertInZone(Zone.DISCARD, theirPhial);
		assertNotAttachedTo(phial, sam);
		assertNotAttachedTo(theirPhial, sam);
		assertEquals(4, scn.GetTwilight());
		assertEquals(4, scn.GetVitality(sam));
		// The Shadow player's copy, in his own discard pile, made him no offer on the way.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheResponseIsNotOfferedWhileThePhialIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(phial);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		scn.FreepsChooseCard(frodo);
		assertInZone(Zone.DEAD, frodo);
		scn.FreepsChooseOptionalTrigger(sam);

		// The Ring has been transferred, and the game has gone on without a word from the Phial.
		assertEquals(sam, scn.GetRingBearer());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(phial));
		assertInZone(Zone.HAND, phial);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheResponseIsNotOfferedWhileThePhialIsAlreadyOnTheNewRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, phial);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		scn.FreepsChooseCard(frodo);
		assertInZone(Zone.DEAD, frodo);
		scn.FreepsChooseOptionalTrigger(sam);

		assertEquals(sam, scn.GetRingBearer());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(phial));
		assertAttachedTo(phial, sam);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TransferringAnotherItemToSamIsNotTheOneRingBeingTransferred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToDiscard(phial);

		scn.StartGame();

		assertAttachedTo(sword, frodo);
		assertTrue(scn.FreepsTransferAvailable(sword));

		scn.FreepsTransferCard(sword);

		assertAttachedTo(sword, sam);
		assertEquals(frodo, scn.GetRingBearer());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(phial));
		assertInZone(Zone.DISCARD, phial);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
