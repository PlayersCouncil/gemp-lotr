package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "10_67");

					put("lemenya", "1_232");        // Úlairë Lemenya, Lieutenant of Morgul: a Nazgûl, strength 9, vitality 2; his text needs a [wraith] condition on a companion
					put("attea", "1_229");          // Úlairë Attëa, Keeper of Dol Guldur: a Nazgûl, vitality 3; fierce, a skirmish ability used only on purpose
					put("darkness", "8_68");        // Beyond All Darkness: Shadow Skirmish event, cost 3, "Exert a Nazgûl to make him strength +2 and damage +1"
					put("charge", "1_223");         // Relentless Charge: Shadow event, cost 0, "Maneuver: Exert a Nazgûl to wound every archer companion"
					put("shagrat", "10_99");        // Shagrat, Captain of Cirith Ungol: a Shadow character; "Each time Shagrat loses a skirmish, you may exert a character"
					put("scout", "1_191");          // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("scimitar", "1_180");       // Goblin Scimitar: a possession (hand weapon) on a [moria] Orc; a play trigger only
					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1; a play trigger only

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");         // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("rider", "4_286");          // Rider of Rohan: strength 5, vitality 3; his ability is used only on purpose
					put("mount", "4_287");          // Rider's Mount: Free Peoples possession, "At the start of each skirmish involving bearer, each minion skirmishing bearer must exert"
					put("narsil", "9_34");          // Narsil, Blade of the Faithful: an ARTIFACT hand weapon on a [gondor] Man; its response is used only on purpose
					put("bow", "1_90");             // Aragorn's Bow: a [gondor] possession, ranged weapon, on Aragorn; makes him an archer
					put("cloak", "1_42");           // Elven Cloak: an [elven] possession, cloak, on a companion; minion archery total -1
					put("poison", "8_28");          // Spider Poison: a [gollum] (Shadow) possession a companion bears; bearer exerts as he is assigned
					put("mail", "1_101");           // Coat of Mail: a [gondor] possession, armor, on a Man
					put("merry", "1_302");          // Merry, Friend to Sam: a skirmish ability used only on purpose
					put("hobbitsword", "1_299");    // Hobbit Sword: a possession (hand weapon) on a Hobbit, no other text
					put("maggot", "1_288");         // Farmer Maggot, Chaser of Rascals: a Hobbit ally, home 1F
					put("follow", "1_109");         // One Whom Men Would Follow: exert Aragorn to have an ally take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireCanteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Úlairë Cantëa, Thrall of The One
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Enduring</b>. <b>Fierce</b>.
		 * Shadow cards cannot exert Úlairë Cantëa during a skirmish phase.
		 * <b>Skirmish:</b> Heal Úlairë Cantëa to discard a possession borne by a character he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cantea");

		assertEquals("Úlairë Cantëa", card.getBlueprint().getTitle());
		assertEquals("Thrall of The One", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowEventExertsAnotherNazgulAndNotCanteaInTheSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var cantea = scn.GetShadowCard("cantea");
		var lemenya = scn.GetShadowCard("lemenya");
		var attea = scn.GetShadowCard("attea");
		var darkness = scn.GetShadowCard("darkness");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, lemenya, attea);
		scn.MoveCardsToHand(darkness);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		// Lemenya and Attëa are left out of the fighting.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, cantea));
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertTrue(scn.ShadowPlayAvailable(darkness));

		scn.ShadowPlayCard(darkness);
		// The event may exert either of the other two Nazgûl, and may not exert Cantëa, who could pay an exertion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, attea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cantea));

		scn.ShadowChooseCard(attea);
		assertEquals(1, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AShadowCharactersEffectInTheSkirmishPhaseMayExertAnotherCharacterButNotCantea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var cantea = scn.GetShadowCard("cantea");
		var lemenya = scn.GetShadowCard("lemenya");
		var shagrat = scn.GetShadowCard("shagrat");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, lemenya, shagrat);
		// Strength that only arranges the board: Aragorn beats Shagrat's 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, shagrat}, new PhysicalCardImpl[]{boromir, cantea});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Shagrat has lost: his "may exert a character" is offered while the skirmish phase is resolved.
		assertEquals(1, scn.GetWoundsOn(shagrat));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shagrat));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(cantea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, boromir, lemenya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cantea));
		scn.ShadowChooseCard(lemenya);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AFreePeoplesCardExertsCanteaInHisSkirmishAndAShadowEventCannot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);

		var cantea = scn.GetShadowCard("cantea");
		var darkness = scn.GetShadowCard("darkness");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		scn.MoveCardsToHand(darkness);
		assertAttachedTo(mount, rider);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, cantea);

		assertEquals(0, scn.GetWoundsOn(cantea));
		scn.FreepsResolveSkirmish(rider);
		// Rider's Mount is a Free Peoples card: at the start of the skirmish it exerts Cantëa all the same.
		assertTrue(scn.IsCharSkirmishing(cantea));
		assertEquals(1, scn.GetWoundsOn(cantea));

		scn.SetTwilight(3);
		scn.FreepsPass();
		// The event's 3 are in the pool and Cantëa, vitality 3 with 1 wound, has an exertion left in him:
		// he is the only Nazgûl, and a Shadow card cannot exert him.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(darkness));
		assertEquals(1, scn.GetWoundsOn(cantea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AShadowCardExertsCanteaInTheManeuverPhaseAndNotInTheSkirmishPhaseAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var cantea = scn.GetShadowCard("cantea");
		var charge = scn.GetShadowCard("charge");
		var darkness = scn.GetShadowCard("darkness");

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		scn.MoveCardsToHand(charge, darkness);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Maneuver: Cantëa is the only Nazgûl, so he is the one who pays for Relentless Charge.
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertTrue(scn.ShadowPlayAvailable(charge));
		scn.ShadowPlayCard(charge);
		assertEquals(1, scn.GetWoundsOn(cantea));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Skirmish: vitality 3 with 1 wound, he could pay one more exertion, and the pool holds the event's 3:
		// 10 set + 2 companions + 2 for the site, and Relentless Charge cost nothing.
		assertEquals(14, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(darkness));
		assertEquals(1, scn.GetWoundsOn(cantea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionHealsCanteaToDiscardAPossessionBorneByTheCompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var scout = scn.GetShadowCard("scout");
		var scimitar = scn.GetShadowCard("scimitar");
		var runner = scn.GetShadowCard("runner");
		var poison = scn.GetShadowCard("poison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var bow = scn.GetFreepsCard("bow");
		var cloak = scn.GetFreepsCard("cloak");
		var merry = scn.GetFreepsCard("merry");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, narsil, cloak);
		scn.AttachCardsTo(merry, hobbitsword);

		scn.StartGame();

		scn.MoveMinionsToTable(cantea, scout, runner);
		scn.AttachCardsTo(scout, scimitar);
		scn.AttachCardsTo(aragorn, poison);
		scn.AddWoundsToChar(cantea, 2);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(hobbitsword, merry);
		assertAttachedTo(scimitar, scout);
		assertEquals(2, scn.GetWoundsOn(cantea));

		scn.SkipToAssignments();
		// Merry's skirmish has not begun when Cantëa acts.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, cantea}, new PhysicalCardImpl[]{merry, runner});
		// The Scout is a second minion in Aragorn's skirmish, bearing a possession of his own.
		scn.ShadowAssignToMinions(aragorn, scout);
		// The bow makes Aragorn an archer: laid after the archery phase.
		scn.AttachCardsTo(aragorn, bow);
		assertAttachedTo(bow, aragorn);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, cantea));
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(cantea));
		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);

		// One wound of two healed.
		assertEquals(1, scn.GetWoundsOn(cantea));
		// Three possessions of other cultures, classes and sides on the companion he skirmishes; not the artifact
		// beside them, a possession on a companion whose skirmish is still to come, or one on a minion in the same
		// skirmish.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bow, cloak, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(narsil, hobbitsword, scimitar));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(cloak);

		assertInDiscard(cloak);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(hobbitsword, merry);
		assertAttachedTo(scimitar, scout);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void APossessionBorneByAnAllyHeIsSkirmishingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(maggot, hobbitsword);
		scn.MoveCardsToHand(follow);

		scn.StartGame();

		scn.MoveMinionsToTable(cantea);
		scn.AddWoundsToChar(cantea, 1);
		assertAttachedTo(hobbitsword, maggot);
		assertEquals(1, scn.GetWoundsOn(cantea));

		// Set-up: Maggot takes part in skirmishes until the regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(maggot, cantea);
		scn.FreepsResolveSkirmish(maggot);
		assertTrue(scn.IsCharSkirmishingAgainst(maggot, cantea));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);

		assertEquals(0, scn.GetWoundsOn(cantea));
		assertInDiscard(hobbitsword);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InAnotherSkirmishCanteaIsHealedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var mail = scn.GetFreepsCard("mail");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, cloak);
		scn.AttachCardsTo(boromir, mail);

		scn.StartGame();

		scn.MoveMinionsToTable(cantea, runner);
		scn.AddWoundsToChar(cantea, 1);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(mail, boromir);
		assertEquals(1, scn.GetWoundsOn(cantea));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, cantea}, new PhysicalCardImpl[]{boromir, runner});
		// Boromir's skirmish is fought first; Aragorn is assigned to Cantëa and waits for his.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner));
		scn.FreepsPass();

		// The cost can be paid, so the ability is offered; nobody is skirmishing Cantëa, so nothing is discarded:
		// not the possession of the companion fighting now, nor that of the companion assigned to him.
		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);

		assertEquals(0, scn.GetWoundsOn(cantea));
		assertAttachedTo(mail, boromir);
		assertAttachedTo(cloak, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileCanteaIsUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();

		scn.MoveMinionsToTable(cantea, lemenya);
		// Lemenya, another Nazgûl, has a wound to heal if the cost were not Cantëa's own.
		scn.AddWoundsToChar(lemenya, 1);
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertAttachedTo(cloak, aragorn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cantea);
		// Lemenya is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, cantea));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));
		assertAttachedTo(cloak, aragorn);
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertEquals(1, scn.GetWoundsOn(lemenya));
	}
}
