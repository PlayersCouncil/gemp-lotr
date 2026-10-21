package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("throw", "2_71");
					put("throw2", "2_71");

					// [moria] Orcs of two titles, for the spot
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1; "When you play this minion, you may add (2)"
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc with no text
					// The Balrog, The Terror of Khazad-dum: a [moria] minion who is no Orc; its text acts only underground
					put("balrog", "12_79");
					put("soldier", "1_271");    // Orc Soldier: an Orc who is not [moria] ([sauron]); a skirmish ability only

					// Bearers and others
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; abilities used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit; the [gondor] companion Pippin needs
					put("bounder", "1_286");    // Bounder: a Hobbit ally, no companion (home 2F); a skirmish ability only

					// Free Peoples cards and a site that exert a Hobbit
					put("sorry", "1_312");      // Sorry About Everything: [shire] Fellowship event, "Exert a Hobbit companion to remove a burden"
					// Sting: a [shire] possession, "Bearer must be Frodo.  Fellowship or Regroup: Exert Frodo to reveal an
					// opponent's hand.  Remove (1) for each Orc revealed (limit (4))"; the Ring-bearer pays for it
					put("sting", "1_313");

					// Shadow cards that exert a Hobbit
					put("skirmisher", "1_257"); // Morgul Skirmisher: a minion, "Maneuver: Remove (3) to exert a Hobbit (except the Ring-bearer)"
					put("resistance", "2_79");  // Resistance Becomes Unbearable: a Shadow event, "Exert a twilight Nazgul to exert the Ring-bearer"
					put("nelya", "2_84");       // Ulaire Nelya: a twilight Nazgul to pay for it; fierce and damage +1 only at 2 burdens or 2 wounds
					put("hammer", "11_130");    // Orc Hammer: a Shadow possession, "When you play this possession, you may exert a companion"
					put("champion", "11_111");  // Champion Orc: an [orc] Orc to bear the Hammer; toil 2 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThrowYourselfinNextTimeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Throw Yourself in Next Time
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, spot a [moria] Orc. Plays on a Hobbit companion.<br>Bearer may be exerted only by Shadow cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("throw");

		assertEquals("Throw Yourself in Next Time", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAMoriaOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throwcard = scn.GetShadowCard("throw");
		var runner = scn.GetShadowCard("runner");
		var balrog = scn.GetShadowCard("balrog");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(throwcard, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog, soldier);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// A [moria] minion who is no Orc and an Orc who is not [moria]: neither is spotted for it, with Frodo there
		// to bear it.
		assertInZone(Zone.SHADOW_CHARACTERS, balrog, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(throwcard));

		scn.ShadowPlayCard(runner);

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.ShadowPlayAvailable(throwcard));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysOnAHobbitCompanionOfEitherCultureAndNotOnAManOrAHobbitAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throwcard = scn.GetShadowCard("throw");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(throwcard);

		scn.StartGame();
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(throwcard));
		scn.ShadowPlayCard(throwcard);

		// The Ring-bearer, a [shire] Hobbit and a [gondor] Hobbit; not the Man nor the Hobbit who is an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, merry, pippin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(pippin);

		assertAttachedTo(throwcard, pippin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFreePeoplesEventMayNotExertTheBearerButExertsAnotherHobbitCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throwcard = scn.GetShadowCard("throw");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var sorry = scn.GetFreepsCard("sorry");
		scn.MoveCardsToHand(sorry);

		scn.StartGame();
		scn.MoveCompanionsToTable(merry, sam);
		// Played on Merry in the Shadow player's last Shadow phase, the condition is still there on this turn.
		scn.AttachCardsTo(merry, throwcard);
		scn.AddBurdens(1);
		assertAttachedTo(throwcard, merry);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsPlayAvailable(sorry));
		scn.FreepsPlayCard(sorry);

		// Merry and Sam are Hobbit companions alike, unwounded; only the bearer is kept out.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFreePeoplesCompanionsAbilityAndAFreePeoplesPossessionsAbilityMayNotExertTheBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throwcard = scn.GetShadowCard("throw");
		var throw2 = scn.GetShadowCard("throw2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");

		scn.StartGame();
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, sting);
		scn.AddBurdens(1);
		assertAttachedTo(sting, frodo);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsRefreshDecision();

		// Without the condition: Sam's "Fellowship: Exert Sam to remove a burden" and Sting's "Exert Frodo" are both on offer.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(sam));
		assertTrue(scn.FreepsActionAvailable(sting));

		// Each now bears the condition: the exerting is done by a Free Peoples companion and a Free Peoples
		// possession, neither a Shadow card.
		scn.AttachCardsTo(sam, throwcard);
		scn.AttachCardsTo(frodo, throw2);
		assertAttachedTo(throwcard, sam);
		assertAttachedTo(throw2, frodo);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sam));
		assertFalse(scn.FreepsActionAvailable(sting));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void ASiteIsNoShadowCardAndMayNotExertTheBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throwcard = scn.GetShadowCard("throw");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");

		scn.StartGame();
		scn.MoveCompanionsToTable(merry, sam);
		scn.AttachCardsTo(merry, throwcard);
		scn.MoveMinionsToTable(troop);
		assertAttachedTo(throwcard, merry);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, troop);
		scn.FreepsResolveSkirmish(sam);

		// Ettenmoors, site 2: "Skirmish: Exert your companion or minion to make that character strength +2."  A
		// site has no side: it is neither a Free Peoples card nor a Shadow card (CR 4.2 "Shadow cards").
		var ettenmoors = scn.GetCurrentSite();
		assertEquals("Ettenmoors", ettenmoors.getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
		scn.FreepsUseCardAction(ettenmoors);

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AShadowMinionsAbilityAndAShadowEventMayStillExertTheBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throwcard = scn.GetShadowCard("throw");
		var throw2 = scn.GetShadowCard("throw2");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var resistance = scn.GetShadowCard("resistance");
		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(resistance);

		scn.StartGame();
		scn.MoveCompanionsToTable(merry, sam);
		scn.AttachCardsTo(merry, throwcard);
		scn.AttachCardsTo(frodo, throw2);
		scn.MoveMinionsToTable(skirmisher, nelya);
		scn.SetTwilight(10);
		assertAttachedTo(throwcard, merry);
		assertAttachedTo(throw2, frodo);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A minion's ability: the bearer is offered beside the other Hobbit, and exerts.
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowActionAvailable(skirmisher));
		scn.ShadowUseCardAction(skirmisher);
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, sam));
		scn.ShadowChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsPass();

		// A Shadow event exerts the Ring-bearer, who bears the second copy.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowPlayAvailable(resistance));
		scn.ShadowPlayCard(resistance);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AShadowPossessionMayStillExertTheBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throwcard = scn.GetShadowCard("throw");
		var hammer = scn.GetShadowCard("hammer");
		var champion = scn.GetShadowCard("champion");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(hammer);

		scn.StartGame();
		scn.MoveCompanionsToTable(merry, sam);
		scn.AttachCardsTo(merry, throwcard);
		scn.MoveMinionsToTable(champion);
		scn.SetTwilight(5);
		assertAttachedTo(throwcard, merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsPass();

		scn.ShadowPlayCard(hammer);
		assertAttachedTo(hammer, champion);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hammer));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, merry, sam));
		scn.ShadowChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
