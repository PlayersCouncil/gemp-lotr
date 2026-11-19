package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_170_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The card is not unique: four copies, for the limit per bearer and for a second bearer.
					put("sense1", "12_170");
					put("sense2", "12_170");
					put("sense3", "12_170");
					put("sense4", "12_170");

					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl; nothing below 2 burdens or 2 wounds on the Ring-bearer
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, strength 5, cost 2; nothing below 2 wounds on the Ring-bearer
					put("hound2", "7_192");
					put("runner", "1_178");     // Goblin Runner: a [moria] minion; its text is a play trigger, and it is cheated in
					put("rider", "12_161");     // Black Rider: a fierce [wraith] Nazgûl, strength 8 +1 for each companion
					put("drawn", "1_211");      // Drawn to Its Power: a [wraith] condition in the support area; answers a companion killed, and none is

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: an unbound [dwarven] Dwarf, strength 6; a skirmish ability never used here
					put("boromir", "1_96");     // Boromir, Lord of Gondor: an unbound [gondor] Man, strength 7; no special ability
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, strength 8, home site 3

					put("dart", "8_69");        // Black Dart: a [wraith] condition an unbound companion bears (cheated on); bearer cannot heal or bear mounts
					put("draught", "9_24");     // Ent Draught: a [gandalf] condition on a companion (cheated on: no Ent to spot); vitality +1
					put("foes", "11_196");      // Our Foes Are Weak: an [uruk-hai] Shadow condition on an unbound companion (cheated on); strength -1, resistance -3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SenseofObligationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Sense of Obligation
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, spot a [wraith] minion. Bearer must be an unbound companion. Limit 1 per bearer.<br>At the end of each assignment phase, if bearer is not assigned to a skirmish and a companion who does not bear a [wraith] condition is, add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sense1");

		assertEquals("Sense of Obligation", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAWraithMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var sense1 = scn.GetShadowCard("sense1");
		var hound = scn.GetShadowCard("hound");
		var runner = scn.GetShadowCard("runner");
		var drawn = scn.GetShadowCard("drawn");
		scn.MoveCardsToHand(sense1, hound);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(drawn);
		scn.SetTwilight(10);

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SUPPORT, drawn);
		scn.FreepsPass();

		// A [moria] minion and a [wraith] card that is no minion: nothing to spot.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sense1));

		// One legal play later, a [wraith] Orc is in play.
		scn.ShadowPlayCard(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertTrue(scn.ShadowPlayAvailable(sense1));

		// Aragorn is the one unbound companion: it goes on him with nothing asked.
		scn.ShadowPlayCard(sense1);
		assertAttachedTo(sense1, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayedOnAnUnboundCompanionAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(elrond);

		var sense1 = scn.GetShadowCard("sense1");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(sense1);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(sense1));
		scn.ShadowPlayCard(sense1);
		// Aragorn and Gimli, unbound companions of two cultures and two races.  Not the Ring-bearer, nor Sam, who is
		// Ring-bound; not Elrond, an ally; not the Nazgûl.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, elrond, nelya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);
		assertAttachedTo(sense1, gimli);
		assertNotAttachedTo(sense1, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionWhoBearsOneIsNoBearerForAnotherAndWithEveryUnboundCompanionBearingOneItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		var sense1 = scn.GetShadowCard("sense1");
		var sense2 = scn.GetShadowCard("sense2");
		var sense3 = scn.GetShadowCard("sense3");
		var sense4 = scn.GetShadowCard("sense4");
		var nelya = scn.GetShadowCard("nelya");
		var dart = scn.GetShadowCard("dart");
		scn.MoveCardsToHand(sense2, sense3, sense4);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, sense1);
		scn.AttachCardsTo(gimli, dart);
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(10);

		assertAttachedTo(sense1, aragorn);
		assertAttachedTo(dart, gimli);
		assertInZone(Zone.HAND, sense4);
		scn.FreepsPass();

		scn.ShadowPlayCard(sense2);
		// Not Aragorn, who bears a copy; Gimli, who bears another [wraith] condition, and Boromir, who bears nothing.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(gimli);
		assertAttachedTo(sense2, gimli);

		// Boromir is now the one companion it may go on.
		scn.ShadowPlayCard(sense3);
		assertAttachedTo(sense3, boromir);

		// Every unbound companion bears one, and a [wraith] minion is still there to spot: the fourth cannot be played.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertInZone(Zone.HAND, sense4);
		assertFalse(scn.ShadowPlayAvailable(sense4));
	}

	@Test
	public void WithBearerUnassignedAndTwoCompanionsAssignedOneBurdenIsAddedAtTheEndOfTheAssignmentPhaseAndNoMoreAsTheSkirmishesStart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		var sense1 = scn.GetShadowCard("sense1");
		var hound = scn.GetShadowCard("hound");
		var hound2 = scn.GetShadowCard("hound2");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, sense1);
		scn.MoveMinionsToTable(hound, hound2);

		assertAttachedTo(sense1, aragorn);
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, hound}, new PhysicalCardImpl[]{boromir, hound2});

		// Aragorn is unassigned and two companions who bear no [wraith] condition are: one burden, not one for each.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(1, scn.GetBurdens());

		// Gimli, 6 against 5, wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.GetBurdens());

		// The second skirmish starts, the board as it was: no further burden.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void WithBearerAssignedNoBurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		var sense1 = scn.GetShadowCard("sense1");
		var hound = scn.GetShadowCard("hound");
		var hound2 = scn.GetShadowCard("hound2");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, sense1);
		scn.MoveMinionsToTable(hound, hound2);

		assertAttachedTo(sense1, aragorn);
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		// Gimli, who bears nothing, is assigned; so is the bearer.  Boromir, who bears nothing either, is not.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, hound}, new PhysicalCardImpl[]{gimli, hound2});

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.IsCharAssigned(boromir));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void NoBurdenIsAddedWhereTheOnlyCharactersAssignedAreACompanionBearingAWraithConditionAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");

		var sense1 = scn.GetShadowCard("sense1");
		var dart = scn.GetShadowCard("dart");
		var hound = scn.GetShadowCard("hound");
		var hound2 = scn.GetShadowCard("hound2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(aragorn, sense1);
		scn.AttachCardsTo(gimli, dart);
		scn.MoveMinionsToTable(hound, hound2);

		assertAttachedTo(sense1, aragorn);
		assertAttachedTo(dart, gimli);
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		// Site 3: Elrond's home, where he may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		// Gimli bears a [wraith] condition, and Elrond, who bears none, is no companion.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, hound}, new PhysicalCardImpl[]{elrond, hound2});

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void TheFierceAssignmentPhaseAddsASecondBurdenForACompanionBearingAConditionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var draught = scn.GetFreepsCard("draught");
		var foes = scn.GetShadowCard("foes");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.AttachCardsTo(boromir, draught);
		scn.AttachCardsTo(gimli, foes);

		var sense1 = scn.GetShadowCard("sense1");
		var rider = scn.GetShadowCard("rider");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, sense1);
		scn.MoveMinionsToTable(rider);

		assertAttachedTo(sense1, aragorn);
		assertAttachedTo(draught, boromir);
		assertAttachedTo(foes, gimli);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.SkipToAssignments();
		// Boromir bears a [gandalf] condition, no [wraith] one.
		scn.FreepsAssignToMinions(boromir, rider);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.GetBurdens());

		// The Black Rider, 8 + 4 for the companions, beats Boromir's 7 without doubling it, and lives on into the fierce round.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.InFierceRound());
		assertEquals(1, scn.GetBurdens());

		scn.PassFierceAssignmentActions();
		// Gimli bears a Shadow condition of another culture, an [uruk-hai] one, no [wraith] one; Boromir is not assigned again.
		scn.FreepsAssignToMinions(gimli, rider);

		// The end of the fierce assignment phase is the end of an assignment phase as well.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(2, scn.GetBurdens());
	}
}
