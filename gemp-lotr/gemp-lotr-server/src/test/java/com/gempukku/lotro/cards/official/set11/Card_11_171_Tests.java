package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_11_171_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("salt", "11_171");
					put("salt2", "11_171");
					put("salt3", "11_171");
					put("salt4", "11_171");

					put("merry", "1_303");        // Merry, From O'er the Brandywine: unbound [shire] Hobbit, strength 3, vitality 4; +2 only with a weapon
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit companion; to play, spot a [gondor] companion
					put("bounder", "1_286");      // Bounder: a [shire] Hobbit ALLY, so no unbound Hobbit (CR 4.2 "unbound")
					put("smeagol", "103_116");    // Smeagol, Once Like Us: a [shire] companion who is no Hobbit

					put("brigand", "4_10");       // Dunlending Brigand: strength 8, vitality 1, no text
					put("swordsman", "3_98");     // Orc Swordsman: strength 9; +2 only while he bears a weapon (he bears none)
					put("seeker", "4_195");       // Uruk Seeker: strength 6, vitality 2, fierce; +2 only against a bearer of a search card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SaltfromtheShireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Salt from the Shire
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Box
		 * Resistance: +2
		 * Game Text: Bearer must be a Hobbit.
		 * 	Fellowship: Discard this possession to remove a burden.
		 * 	Skirmish: If bearer is an unbound Hobbit, discard this possession to prevent him or her from being
		 * 	overwhelmed unless his or her strength is tripled.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("salt");

		assertEquals("Salt from the Shire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.BOX));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysOnAHobbitCompanionOrAllyOfEitherCultureAndNotOnACompanionWhoIsNoHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salt = scn.GetFreepsCard("salt");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(salt);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(salt));
		scn.FreepsPlayCard(salt);
		// The Ring-bearer, a [gondor] Hobbit and a Hobbit ally may bear it; a [shire] companion who is no Hobbit may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(pippin);
		assertAttachedTo(salt, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsThatCopyToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salt = scn.GetFreepsCard("salt");
		var salt2 = scn.GetFreepsCard("salt2");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, salt);
		scn.AttachCardsTo(frodo, salt2);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		assertAttachedTo(salt, merry);
		assertAttachedTo(salt2, frodo);
		assertTrue(scn.FreepsActionAvailable(salt));

		scn.FreepsUseCardAction(salt);
		// The copy whose action was used goes, borne by someone other than the Ring-bearer; the other stays.
		assertInDiscard(salt);
		assertAttachedTo(salt2, frodo);
		// 2 - 1
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salt = scn.GetFreepsCard("salt");
		var merry = scn.GetFreepsCard("merry");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, salt);

		scn.StartGame();
		scn.AddBurdens(1);
		assertTrue(scn.FreepsActionAvailable(salt));

		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(salt));
		assertAttachedTo(salt, merry);
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void SkirmishActionKeepsAnUnboundHobbitFromBeingOverwhelmedByDoubleHisStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salt = scn.GetFreepsCard("salt");
		var merry = scn.GetFreepsCard("merry");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, salt);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, brigand);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertAttachedTo(salt, merry);
		assertTrue(scn.FreepsActionAvailable(salt));

		scn.FreepsUseCardAction(salt);
		assertInDiscard(salt);

		scn.PassSkirmishActions();
		// 8 is double his 3 but not triple: he loses and takes the Brigand's one wound instead of dying.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedOnARingBoundHobbitOrAHobbitAllyAndIsOnAnUnboundHobbitOutsideTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salt = scn.GetFreepsCard("salt");
		var salt2 = scn.GetFreepsCard("salt2");
		var salt3 = scn.GetFreepsCard("salt3");
		var salt4 = scn.GetFreepsCard("salt4");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(sam, merry, pippin);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(sam, salt);
		scn.AttachCardsTo(merry, salt2);
		scn.AttachCardsTo(bounder, salt3);
		scn.AttachCardsTo(pippin, salt4);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, brigand);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Sam is a Hobbit companion, but Ring-bound; Bounder is a Hobbit, but an ally, and only companions are unbound.
		assertFalse(scn.FreepsActionAvailable(salt));
		assertFalse(scn.FreepsActionAvailable(salt3));
		// Merry is an unbound Hobbit: the text asks nothing of where he is, so his copy is offered in Sam's skirmish.
		// So is Pippin's, an unbound Hobbit of another culture ([gondor], where Merry is [shire]).
		assertTrue(scn.FreepsActionAvailable(salt2));
		assertTrue(scn.FreepsActionAvailable(salt4));
		assertAttachedTo(salt, sam);
		assertAttachedTo(salt2, merry);
		assertAttachedTo(salt3, bounder);
		assertAttachedTo(salt4, pippin);
	}

	@Test
	public void AStrengthTripledStillOverwhelmsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salt = scn.GetFreepsCard("salt");
		var merry = scn.GetFreepsCard("merry");
		var swordsman = scn.GetShadowCard("swordsman");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, salt);

		scn.StartGame();
		scn.MoveMinionsToTable(swordsman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, swordsman);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(9, scn.GetStrength(swordsman));
		assertAttachedTo(salt, merry);
		scn.FreepsUseCardAction(salt);
		assertInDiscard(salt);

		scn.PassSkirmishActions();
		// 9 is three times 3.
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheProtectionEndsWithTheSkirmishItWasUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var salt = scn.GetFreepsCard("salt");
		var merry = scn.GetFreepsCard("merry");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, salt);

		scn.StartGame();
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, seeker);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(6, scn.GetStrength(seeker));
		assertAttachedTo(salt, merry);
		scn.FreepsUseCardAction(salt);
		scn.PassSkirmishActions();
		// 6 is double his 3: in the skirmish it was used in he only loses, and takes one wound.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// The Seeker is fierce: the same 6 against 3 in the fierce skirmish, a skirmish phase of its own.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(merry, seeker);
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.InFierceRound());
		assertEquals(3, scn.GetStrength(merry));
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
