package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mantle", "103_3");
					put("gandalf", "1_72");      // Gandalf, Friend of the Shirefolk: vitality 4; a strength modifier and nothing else
					put("radagast", "9_26");     // Radagast, The Brown: a [gandalf] Wizard who is not Gandalf; move limit +1
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability never used here
					put("gimli", "1_13");        // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability
					put("bounder", "1_286");     // Bounder: Hobbit ally; a skirmish ability

					put("staff", "7_38");        // Gandalf's Staff, Focus of Power: [gandalf] artifact; strength +1 with 4 twilight
					put("narya", "3_34");        // Narya: [gandalf] artifact; an offer at the start of each turn
					put("glamdring", "1_75");    // Glamdring: [gandalf] possession, and no artifact
					put("starglass", "10_13");   // Phial of Galadriel, Star-glass: [elven] artifact, bearer a Hobbit; a skirmish ability
					put("toby", "1_305");        // Old Toby: [shire] possession that sits in the support area

					put("treachery", "1_86");    // Treachery Deeper Than You Know: Spell. Fellowship: Spot Gandalf to reveal an opponent's hand.
					put("salve", "12_32");       // Salve: Spell, and a condition borne by a companion. To play, spot a [gandalf] Wizard.
					put("folly", "1_77");        // Let Folly Be Our Cloak: a [gandalf] event and no spell. Exert X companions to remove (X).

					// Cruel Caradhras: an [isengard] spell. Maneuver: Exert an [isengard] minion to make the opponent choose
					// to either exert the Ring-bearer or add a burden.
					put("caradhras", "1_124");
					put("savage", "1_151");      // Uruk Savage: [isengard] minion, vitality 3; damage +1 and nothing else
					put("armory", "1_157");      // Uruk-hai Armory: Shadow condition; does nothing without an Uruk-hai
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MantleoftheWhiteWizardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Mantle of the White Wizard
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time you play a spell, you may hinder a [Gandalf] artifact to restore a Free Peoples card.
		 * 	Each time the fellowship moves to or from a sanctuary, you may exert Gandalf to heal another companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mantle");

		assertEquals("Mantle of the White Wizard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(mantle);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(mantle));

		scn.FreepsPlayCard(mantle);

		assertInZone(Zone.SUPPORT, mantle);
		assertNotAttachedTo(mantle, gandalf);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachSpellPlayedMayHinderAGandalfArtifactToRestoreAFreePeoplesCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var staff = scn.GetFreepsCard("staff");
		var narya = scn.GetFreepsCard("narya");
		var glamdring = scn.GetFreepsCard("glamdring");
		var starglass = scn.GetFreepsCard("starglass");
		var toby = scn.GetFreepsCard("toby");
		var treachery = scn.GetFreepsCard("treachery");
		var salve = scn.GetFreepsCard("salve");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(mantle, toby);
		scn.MoveCardsToSupportArea(armory);
		scn.AttachCardsTo(gandalf, staff, glamdring);
		scn.AttachCardsTo(frodo, starglass);
		scn.MoveCardsToHand(treachery, salve);
		// A card for Treachery to reveal
		scn.MoveCardsToShadowHand("savage");

		scn.StartGame();
		// Narya goes on after the start of the turn, when it would have made its own offer.
		scn.AttachCardsTo(gandalf, narya);
		// A [gondor] companion, a [shire] possession and a Shadow condition, all hindered.
		scn.HinderCard(aragorn, toby, armory);

		scn.FreepsPlayCard(treachery);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsAcceptOptionalTrigger();

		// The Staff and Narya are [gandalf] artifacts. Glamdring is a [gandalf] possession, the Star-glass an [elven] artifact.
		assertTrue(scn.FreepsHasCardChoiceAvailable(staff, narya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(glamdring, starglass));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.IsHindered(staff));

		scn.FreepsChooseCard(staff);

		assertTrue(scn.IsHindered(staff));
		assertFalse(scn.IsHindered(narya));
		// Any hindered Free Peoples card, the Staff just hindered among them. Not the hindered Shadow card, and not a
		// card that is not hindered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, toby, staff));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory, narya, gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(aragorn);

		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.IsHindered(staff));
		assertTrue(scn.IsHindered(armory));

		// Treachery then does what it says.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second spell, this one a condition: the Mantle offers again.
		scn.FreepsPlayCard(salve);
		scn.FreepsChooseCard(frodo);

		assertAttachedTo(salve, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsAcceptOptionalTrigger();

		// Narya is the one [gandalf] artifact left to hinder.
		assertTrue(scn.IsHindered(narya));
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, staff, narya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory, aragorn));

		scn.FreepsChooseCard(staff);

		assertFalse(scn.IsHindered(staff));
		assertTrue(scn.IsHindered(narya));
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheSpellOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var staff = scn.GetFreepsCard("staff");
		var treachery = scn.GetFreepsCard("treachery");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(mantle);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(treachery);
		scn.MoveCardsToShadowHand("savage");

		scn.StartGame();
		scn.HinderCard(aragorn);

		scn.FreepsPlayCard(treachery);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(staff));
		assertTrue(scn.IsHindered(aragorn));

		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.DISCARD, treachery);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NeitherAnEventOfMineThatIsNoSpellNorAnOpponentsSpellSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var staff = scn.GetFreepsCard("staff");
		var folly = scn.GetFreepsCard("folly");
		var caradhras = scn.GetShadowCard("caradhras");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(mantle);
		// An artifact to hinder and a card to restore, so that the offer would be made for a spell of mine.
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(folly);
		scn.MoveCardsToHand(caradhras);

		scn.StartGame();
		scn.HinderCard(aragorn);
		scn.MoveMinionsToTable(savage);

		// A [gandalf] event with no Spell keyword
		scn.FreepsPlayCard(folly);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsChooseCard(gandalf);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.DISCARD, folly);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 3 set + 2 companions (Aragorn, hindered, adds none) + 2 for the shadow number of site 2:
		// enough for Cruel Caradhras at 2
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(caradhras));

		// A spell, played by the Shadow player
		scn.ShadowPlayCard(caradhras);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mantle));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mantle));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));

		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.IsHindered(staff));
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoGandalfArtifactLeftToHinderASpellBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var staff = scn.GetFreepsCard("staff");
		var glamdring = scn.GetFreepsCard("glamdring");
		var starglass = scn.GetFreepsCard("starglass");
		var treachery = scn.GetFreepsCard("treachery");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(mantle);
		scn.AttachCardsTo(gandalf, staff, glamdring);
		scn.AttachCardsTo(frodo, starglass);
		scn.MoveCardsToHand(treachery);
		scn.MoveCardsToShadowHand("savage");

		scn.StartGame();
		// The one [gandalf] artifact is hindered already. A [gandalf] possession and an [elven] artifact are not.
		scn.HinderCard(staff, aragorn);

		scn.FreepsPlayCard(treachery);

		// Treachery goes straight to its reveal.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.IsHindered(staff));
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(glamdring));
		assertFalse(scn.IsHindered(starglass));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingElseHinderedTheOfferIsStillMadeAndTheArtifactHinderedIsTheOneRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var narya = scn.GetFreepsCard("narya");
		var treachery = scn.GetFreepsCard("treachery");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(mantle);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(treachery);
		scn.MoveCardsToShadowHand("savage");

		scn.StartGame();
		scn.AttachCardsTo(gandalf, narya);

		scn.FreepsPlayCard(treachery);

		// It has a cost that can be paid, so it is offered.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(staff, narya));
		scn.FreepsChooseCard(staff);

		// The Staff was then the only hindered Free Peoples card, and so the one restored.
		assertFalse(scn.IsHindered(staff));
		assertFalse(scn.IsHindered(narya));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MovingToASanctuaryAndAgainMovingFromItMayExertGandalfToHealAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gandalf, aragorn, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(mantle);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(bounder, 1);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// The fellowship moves to site 3, Rivendell Valley, a sanctuary.
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		// A wounded Man and a wounded Dwarf. Not Gandalf, now wounded himself; not the wounded ally; not Frodo, unwounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, bounder, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minion comes down, and in the regroup phase the fellowship moves again: from the sanctuary to site 4.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, bounder));

		scn.FreepsChooseCard(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingBetweenSitesThatAreNotSanctuariesBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(mantle);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);

		assertEquals(1, scn.GetCurrentSiteNumber());

		// From site 1 to site 2
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mantle));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSanctuaryOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(mantle);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(aragorn, 1);

		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithGandalfExhaustedTheSanctuaryOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(mantle);

		scn.StartGame();
		scn.SkipToSite(2);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(gandalf, 3);
		scn.AddWoundsToChar(aragorn, 1);

		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mantle));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherWizardCannotPayForGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Radagast is a [gandalf] Wizard able to exert. Gandalf is not in play.
		scn.MoveCompanionsToTable(radagast, aragorn);
		scn.MoveCardsToSupportArea(mantle);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(aragorn, 1);

		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(mantle));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNobodyElseWoundedGandalfMayStillExertAndIsNotTheOneHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mantle = scn.GetFreepsCard("mantle");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(mantle);

		scn.StartGame();
		scn.SkipToSite(2);

		scn.FreepsPass();

		// It has a cost that can be paid, so it is offered.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsAcceptOptionalTrigger();

		// Gandalf is now the only wounded companion, and "another companion" is not him.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
