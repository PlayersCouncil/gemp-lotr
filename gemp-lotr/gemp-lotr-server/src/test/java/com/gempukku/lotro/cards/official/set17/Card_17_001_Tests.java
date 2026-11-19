package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("khazad", "17_1");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli bearing Armor of Khazad, with three other [dwarven] possessions in the discard pile.
	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("khazad", "17_1");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession
					put("helm", "1_15");        // Gimli's Helm: [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Dwarves to bear it, a Man who cannot, and a minion to beat them.
	protected VirtualTableScenario GetBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("khazad1", "17_1");
					put("khazad2", "17_1");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; his skirmish ability is not used
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, strength 5, vitality 3; +2 against an Orc only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man; his maneuver ability is not used
					// Uruk-hai Raiding Party: strength 9, damage +1 and nothing else.  A Dwarf of strength 6 or 5
					// loses to it and takes two wounds, one in each round.
					put("party", "1_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// [dwarven] possessions of three classes in the discard pile, and a card that fails each word beside them.
	protected VirtualTableScenario GetEveryKindInTheDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("khazad", "17_1");
					put("gimli", "1_13");
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession, hand weapon
					put("handaxe", "2_10");     // Hand Axe: [dwarven] possession, hand weapon
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession, armor
					put("guile", "9_8");        // Ring of Guile: [dwarven] artifact, no possession
					put("heart", "1_10");       // Dwarven Heart: [dwarven] condition
					put("gondorarmor", "1_92"); // Armor: [gondor] possession
					put("bracers", "2_3");      // Dwarven Bracers: [dwarven] possession, laid in the Shadow player's own pile
					put("troop", "1_177");      // Goblin Patrol Troop: no text; there so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmorofKhazadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Armor of Khazâd
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a Dwarf.<br>Each time bearer is about to take a wound, you may add a threat to prevent that.<br><b>Regroup:</b> Discard this possession to take up to 2 other [dwarven] possessions into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("khazad");

		assertEquals("Armor of Khazâd", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// "Take up to 2 other [dwarven] possessions into hand from your discard pile": none, one or two (the shape
	// of F-0033, Gwaihir 101_15).  The definition asked for exactly 2 whenever two or more were there.
	// F-0457.
	@Test
	public void UpToTwoPossessionsMayBeOneOrNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var khazad = scn.GetFreepsCard("khazad");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var helm = scn.GetFreepsCard("helm");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, khazad);
		scn.MoveCardsToDiscard(axe, armor, helm);

		scn.StartGame();
		assertAttachedTo(khazad, gimli);
		assertInDiscard(axe, armor, helm);
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(khazad));
		scn.FreepsUseCardAction(khazad);

		// The three others are offered, and not the Armor of Khazad just discarded.
		assertInZone(Zone.DISCARD, khazad);
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(axe);

		assertInZone(Zone.HAND, axe);
		assertInZone(Zone.DISCARD, khazad, armor, helm);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void BearerMustBeADwarfCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var khazad = scn.GetFreepsCard("khazad1");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(khazad);

		scn.StartGame();

		assertInHand(khazad);
		assertTrue(scn.FreepsPlayAvailable(khazad));
		scn.FreepsPlayCard(khazad);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(khazad, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBearerIsAboutToTakeAWoundAThreatPreventsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var khazad1 = scn.GetFreepsCard("khazad1");
		var khazad2 = scn.GetFreepsCard("khazad2");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, khazad1);
		// The other copy, on another Dwarf, is not his armor.
		scn.AttachCardsTo(farin, khazad2);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		assertAttachedTo(khazad1, gimli);
		assertAttachedTo(khazad2, farin);
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, party);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli loses: the first of his two wounds.
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(khazad1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(khazad2));
		scn.FreepsChooseOptionalTrigger(khazad1);

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Damage +1: the second wound is offered again.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(khazad1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(khazad2));
		scn.FreepsChooseOptionalTrigger(khazad1);

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertAttachedTo(khazad1, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundToAnotherDwarfIsNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var khazad = scn.GetFreepsCard("khazad1");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, khazad);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		assertAttachedTo(khazad, gimli);
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, party);
		scn.FreepsResolveSkirmish(farin);
		scn.PassSkirmishActions();

		// Farin, a Dwarf the armor could be played on, bears none: both his wounds are taken, nothing asked.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(khazad));
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesTheWoundsAndAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var khazad = scn.GetFreepsCard("khazad1");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, khazad);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		assertAttachedTo(khazad, gimli);
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, party);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(khazad));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(khazad));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreatsAtTheLimitNothingIsOfferedAndTheWoundsAreTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var khazad = scn.GetFreepsCard("khazad1");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, khazad);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		assertAttachedTo(khazad, gimli);
		// Frodo, Gimli and Farin: three companions, so three threats are as many as there can be.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, party);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(khazad));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityTakesOnlyOtherDwarvenPossessionsFromItsOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEveryKindInTheDiscardPileScenario();

		var khazad = scn.GetFreepsCard("khazad");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var handaxe = scn.GetFreepsCard("handaxe");
		var armor = scn.GetFreepsCard("armor");
		var guile = scn.GetFreepsCard("guile");
		var heart = scn.GetFreepsCard("heart");
		var gondorarmor = scn.GetFreepsCard("gondorarmor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, khazad);
		scn.MoveCardsToDiscard(axe, handaxe, armor, guile, heart, gondorarmor);

		var bracers = scn.GetShadowCard("bracers");
		scn.MoveCardsToDiscard(bracers);

		scn.StartGame();
		assertAttachedTo(khazad, gimli);
		assertInDiscard(axe, handaxe, armor, guile, heart, gondorarmor, bracers);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(khazad));
		scn.FreepsUseCardAction(khazad);

		// The armor is discarded first, and is no "other" possession; nor are the artifact, the condition, the
		// [gondor] possession, or the Shadow player's Bracers in his own pile.
		assertInDiscard(khazad);
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, handaxe, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(khazad, guile, heart, gondorarmor, bracers));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(handaxe, armor);

		assertInHand(handaxe, armor);
		assertInDiscard(axe, khazad, guile, heart, gondorarmor, bracers);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhaseAndWithNothingToTakeIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEveryKindInTheDiscardPileScenario();

		var khazad = scn.GetFreepsCard("khazad");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, khazad);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(khazad, gimli);
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(khazad));

		// Nobody is assigned to the Troop.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(khazad));
		scn.FreepsUseCardAction(khazad);

		assertInDiscard(khazad);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
