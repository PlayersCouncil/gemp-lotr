package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hates", "7_77");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeHatesThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: We Hates Them
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be Sméagol.<br>Each minion gains this ability: "<b>Assignment:</b> Assign this minion to Sméagol."<br><b>Regroup:</b> Discard this condition to discard a minion and play a companion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hates");

		assertEquals("We Hates Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hates", "7_77");
					put("smeagol", "5_28");     // Smeagol, Old Noser: a regroup ability, not used here
					// Reckless Pride: the same ability for its own bearer, a [dwarven] companion
					put("pride", "7_13");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability, not used here
					put("troop1", "1_177");     // Goblin Patrol Troop: no text at all
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The ability this card gives was shown to the Shadow player in Reckless Pride 7_13's words (F-0352): with both
	// conditions in play each minion had two actions that read alike.  The ability costs nothing, so it is not
	// offered where it would assign nobody (Ketura on Q-0034, F-0353; CR 4.2 "assignment phase": "Many assignment
	// actions assign a minion to a companion.  You cannot do this unless both of them are unassigned").
	@Test
	public void EachMinionMayAssignItselfToSmeagolAndNotOnceEitherIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var hates = scn.GetFreepsCard("hates");
		var smeagol = scn.GetFreepsCard("smeagol");
		var pride = scn.GetFreepsCard("pride");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(smeagol, gimli);
		scn.AttachCardsTo(smeagol, hates);
		scn.AttachCardsTo(gimli, pride);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Each minion has the ability, in the words of this card: the two abilities are told apart by whom they name.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(troop1, "Sméagol"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Sméagol"));
		assertTrue(scn.ShadowActionAvailable(troop1, "Reckless Pride"));
		assertFalse(scn.IsCharAssigned(smeagol));
		assertFalse(scn.IsCharAssigned(troop1));

		scn.ShadowUseCardAction(troop1, "Sméagol");

		assertTrue(scn.IsCharAssignedAgainst(smeagol, troop1));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(troop2));

		scn.FreepsPass();

		// The assigned minion has the ability no longer, and neither has the other minion now that Smeagol is
		// assigned.  Reckless Pride's, whose bearer is not, is still the unassigned minion's to use.
		assertFalse(scn.ShadowActionAvailable(troop1, "Sméagol"));
		assertFalse(scn.ShadowActionAvailable(troop2, "Sméagol"));
		assertFalse(scn.ShadowActionAvailable(troop1, "Reckless Pride"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Reckless Pride"));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hates", "7_77");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] Sméagol; a skirmish ability used only on purpose
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] Sméagol; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Sméagol; a maneuver ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a companion who is no Sméagol; a strength bonus only
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PlaysOnSmeagolAndNotOnAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var hates = scn.GetFreepsCard("hates");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(hates);
		assertInHand(hates);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(hates));
		scn.FreepsPlayCard(hates);

		// Sméagol is the one bearer it may have, and it goes on him unasked; Aragorn and Frodo are none.
		assertAttachedTo(hates, smeagol);
		assertNotAttachedTo(hates, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnTheShireSmeagolAndAMinionMayAssignItselfToHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var hates = scn.GetFreepsCard("hates");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(hates);
		assertInHand(hates);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		// This Smeagol is [shire]: the text names only the title.
		assertEquals(Culture.SHIRE, smeagol.getBlueprint().getCulture());
		assertTrue(scn.FreepsPlayAvailable(hates));
		scn.FreepsPlayCard(hates);
		assertAttachedTo(hates, smeagol);

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// An [isengard] Uruk-hai has the ability too, and is assigned to him.
		assertFalse(scn.IsCharAssigned(smeagol));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(savage, "Sméagol"));
		scn.ShadowUseCardAction(savage, "Sméagol");

		assertTrue(scn.IsCharAssignedAgainst(smeagol, savage));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoSmeagolInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var hates = scn.GetFreepsCard("hates");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(hates);
		assertInHand(hates);

		scn.StartGame();

		assertInHand(hates);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hates));
	}

	protected VirtualTableScenario GetRegroupScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hates", "7_77");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] Sméagol; a skirmish ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; a strength bonus only
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] Man ally, who is no companion; a fellowship ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion, laid in the other player's discard pile only

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, no text
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai; damage +1 and nothing else
					// Goblin Scimitar: a [moria] possession borne by an Orc, a Shadow card that is no minion; its text is
					// about being played (cheated on)
					put("scimitar", "1_180");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RegroupAbilityDiscardsItToDiscardAMinionAndPlayACompanionFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var hates = scn.GetFreepsCard("hates");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var boromir = scn.GetShadowCard("boromir");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, hates);
		scn.MoveCardsToDiscard(aragorn, gandalf, barliman, boromir);
		assertAttachedTo(hates, smeagol);
		assertInDiscard(aragorn, gandalf, barliman, boromir);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.AttachCardsTo(troop, scimitar);
		assertAttachedTo(scimitar, troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hates));
		scn.FreepsUseCardAction(hates);

		// The cost: this condition, to the discard pile.
		assertInDiscard(hates);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);

		// A minion of either culture and race; not the Shadow possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scimitar));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertInDiscard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		// Companions of two cultures and races from his own pile; not an ally, nor a companion in the other
		// player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInDiscard(gandalf, barliman, boromir);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionInPlayTheRegroupAbilityStillPlaysACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var hates = scn.GetFreepsCard("hates");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, hates);
		scn.MoveCardsToDiscard(aragorn, gandalf);
		assertAttachedTo(hates, smeagol);
		assertInDiscard(aragorn, gandalf);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// Nothing for "discard a minion" to act on: the play from the discard pile is still made.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hates));
		scn.FreepsUseCardAction(hates);

		assertInDiscard(hates);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gandalf));
		scn.FreepsChooseCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInDiscard(aragorn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	// Ketura on Q-0062: a play from a pile as an effect is a cost, and an action that cannot make it is not offered.
	@Test
	public void RegroupAbilityIsNotOfferedWithNoCompanionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var hates = scn.GetFreepsCard("hates");
		var smeagol = scn.GetFreepsCard("smeagol");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, hates);
		// An ally, who is no companion, is all the pile holds.
		scn.MoveCardsToDiscard(barliman);
		assertAttachedTo(hates, smeagol);
		assertInDiscard(barliman);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		// A minion that could be discarded.
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hates));
		assertAttachedTo(hates, smeagol);
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var hates = scn.GetFreepsCard("hates");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, hates);
		scn.MoveCardsToDiscard(aragorn);
		assertAttachedTo(hates, smeagol);
		assertInDiscard(aragorn);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hates));
		assertAttachedTo(hates, smeagol);
	}
}
