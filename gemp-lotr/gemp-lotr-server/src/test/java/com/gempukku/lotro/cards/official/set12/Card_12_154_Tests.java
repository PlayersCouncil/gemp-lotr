package com.gempukku.lotro.cards.official.set12;

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

public class Card_12_154_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slaughterer", "12_154");
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard and no Uruk-hai, strength 8; each companion
					// is resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");
					put("cirith", "10_81");     // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, whose whole text is Damage +1
					// Orkish Skirmisher: [orc] Orc, strength 9, vitality 3; each time he wins a skirmish he may exert twice to wound a companion
					put("skirmisher", "18_90");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; his response needs Frodo to die
					put("smeagol", "7_71");     // Sméagol, Always Helps: [gollum] companion, strength 3; his assignment ability is used only on purpose
					put("deadcity", "7_56");    // The Dead City: on Sméagol, "if Sméagol is about to be killed in a skirmish, he is discarded instead"
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ALLY, strength 4, vitality 2; takes part in skirmishes by his own maneuver ability
					for (int i = 1; i <= 3; i++) {
						put("filler" + i, "1_177");     // Goblin Patrol Troop: no text at all; cards for the Shadow player's hand
					}
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Uruk Slaughterer and a second [uruk-hai] minion against two Dwarf Guards, each overwhelmed and killed.
	protected VirtualTableScenario GetKillScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slaughterer", "12_154");
					// Suppressing Uruk: [uruk-hai] Uruk-hai, strength 11; its text lowers resistance and nothing else
					put("suppressing", "12_147");
					put("guard1", "1_7");       // Dwarf Guard: strength 4, vitality 2
					put("guard2", "1_7");
					put("scout", "15_116");     // Scouting Orc: [orc] Orc; "Skirmish: Exert this minion to wound a companion it is skirmishing."
					for (int i = 1; i <= 5; i++) {
						put("filler" + i, "1_177");     // Goblin Patrol Troop: no text at all
					}
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukSlaughtererStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Uruk Slaughterer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time a companion is killed in a skirmish involving an [uruk-hai] minion, you may discard any number of cards from hand to draw the same number of cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("slaughterer");

		assertEquals("Uruk Slaughterer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	// "You may discard any number of cards from hand to draw the same number of cards": the discard stands
	// before the "to", and on an optional trigger with nothing else to pay it is of at least one card (the shape
	// of F-0300, Robin Smallburrow 18_111).  The definition asked for any number from none: the offer was made
	// with an empty hand, and once taken could be answered with no card, a second way of saying no.
	// F-0449.
	@Test
	public void TheOfferIsNotMadeWithAnEmptyHandAndOnceTakenDiscardsAtLeastOneCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKillScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);

		var slaughterer = scn.GetShadowCard("slaughterer");
		var suppressing = scn.GetShadowCard("suppressing");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");

		scn.StartGame();
		scn.MoveMinionsToTable(slaughterer, suppressing);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, slaughterer}, new PhysicalCardImpl[]{guard2, suppressing});

		// The first Dwarf is killed with no card in the Shadow player's hand, and cards in his draw deck.
		assertEquals(0, scn.GetShadowHandCount());
		scn.FreepsResolveSkirmish(guard1);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The second is killed by the other [uruk-hai] minion, with two cards in that hand.
		scn.SetShadowHand(filler1, filler2);
		scn.FreepsResolveSkirmish(guard2);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(slaughterer));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		int deck = scn.GetShadowDeckCount();
		scn.ShadowChooseCards(filler1, filler2);

		// Two discarded and two drawn.
		assertInZone(Zone.DISCARD, filler1, filler2);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// "Killed in a skirmish involving an [uruk-hai] minion" is any kill in that skirmish, whatever wound dealt it,
	// and not "killed by an [uruk-hai] minion" (CR 4.2's definition of that is for "<X> kills", Q-0328). The card
	// was built with `killer: culture(uruk-hai),minion`: this kill, by another minion's own wound, was not
	// answered. F-0547.
	@Test
	public void ACompanionKilledInTheSkirmishByAnotherMinionsWoundOffersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKillScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(guard1);

		var slaughterer = scn.GetShadowCard("slaughterer");
		var scout = scn.GetShadowCard("scout");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");

		scn.StartGame();
		scn.AddWoundsToChar(guard1, 1);
		scn.MoveMinionsToTable(slaughterer, scout);
		scn.SetShadowHand(filler1, filler2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, slaughterer);
		// The Shadow player puts the [orc] Orc in the same skirmish.
		scn.ShadowAssignToMinions(guard1, scout);
		scn.FreepsResolveSkirmish(guard1);

		scn.FreepsPass();
		scn.ShadowUseCardAction(scout);
		// The Orc's wound kills the Dwarf before the skirmish is resolved.
		assertInZone(Zone.DEAD, guard1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(slaughterer));
		scn.ShadowAcceptOptionalTrigger();

		int deck = scn.GetShadowDeckCount();
		scn.ShadowChooseCards(filler1, filler2);
		assertInZone(Zone.DISCARD, filler1, filler2);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		// The Dwarf was the skirmish's only Free Peoples character: it is over, and so is the phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachCompanionKilledInASkirmishInvolvingAnUrukhaiMinionOffersTheDiscardToDrawAsMany() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var slaughterer = scn.GetShadowCard("slaughterer");
		var saruman = scn.GetShadowCard("saruman");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");
		var filler3 = scn.GetShadowCard("filler3");

		scn.StartGame();
		// Aragorn has one vitality left: the wound for losing kills him.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(slaughterer, saruman);
		scn.SetShadowHand(filler1, filler2, filler3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetShadowHandCount());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, slaughterer}, new PhysicalCardImpl[]{sam, saruman});

		// The Slaughterer, an [uruk-hai] Uruk-hai, beats a [gondor] Man, and the wound kills him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(slaughterer));
		scn.ShadowAcceptOptionalTrigger();

		// Any number of the three cards in hand, at least one.
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		int deck = scn.GetShadowDeckCount();
		scn.ShadowChooseCards(filler1);
		// One discarded, one drawn.
		assertInZone(Zone.DISCARD, filler1);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Saruman, an [uruk-hai] minion who is no Uruk-hai, overwhelms a [shire] Hobbit, 8 against 3: a second
		// offer, declined, and nothing changes.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(slaughterer));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertInHand(filler2, filler3);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledInAnotherMinionsSkirmishWhileTheSlaughtererWaitsForItsOwnIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var slaughterer = scn.GetShadowCard("slaughterer");
		var cirith = scn.GetShadowCard("cirith");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");

		scn.StartGame();
		// Each companion has one vitality left.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(slaughterer, cirith);
		scn.SetShadowHand(filler1, filler2);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetShadowHandCount());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, slaughterer}, new PhysicalCardImpl[]{sam, cirith});

		// A [sauron] Uruk-hai beats Sam while the Slaughterer is assigned and waiting: no [uruk-hai] minion is in
		// that skirmish.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The control on the same board: the Slaughterer's own skirmish kills Aragorn.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(slaughterer));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionDiscardedInsteadOfKilledInTheSlaughterersSkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var deadcity = scn.GetFreepsCard("deadcity");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, deadcity);

		var slaughterer = scn.GetShadowCard("slaughterer");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");

		scn.StartGame();
		scn.MoveMinionsToTable(slaughterer);
		scn.SetShadowHand(filler1, filler2);
		assertAttachedTo(deadcity, smeagol);
		assertEquals(2, scn.GetShadowHandCount());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, slaughterer);

		// 10 against 3 overwhelms Sméagol, and The Dead City has him discarded instead of killed.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		assertInDiscard(smeagol);
		assertNotInZone(Zone.DEAD, smeagol);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyKilledInTheSlaughterersSkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var slaughterer = scn.GetShadowCard("slaughterer");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");

		scn.StartGame();
		scn.MoveMinionsToTable(slaughterer);
		scn.SetShadowHand(filler1, filler2);
		assertEquals(2, scn.GetShadowHandCount());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, slaughterer);

		// 10 against 4: the ally is overwhelmed and killed in the Slaughterer's skirmish, and he is no companion.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionInNoSkirmishKilledDuringTheSlaughterersSkirmishIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var slaughterer = scn.GetShadowCard("slaughterer");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");

		scn.StartGame();
		// Sam has one vitality left and will be in no skirmish.
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(slaughterer, skirmisher);
		scn.SetShadowHand(filler1, filler2);
		// Aragorn at 10 is beaten by 10 + 9 without being overwhelmed (19 is less than 20).
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetShadowHandCount());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, slaughterer);
		scn.ShadowAssignToMinions(aragorn, skirmisher);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// The wound for losing and the Slaughterer's damage +1.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The Skirmisher's win, in the Slaughterer's skirmish: he exerts twice to wound Sam, who dies.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		scn.ShadowChooseCard(sam);
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(slaughterer));
		scn.ShadowAcceptOptionalTrigger();
		int deck = scn.GetShadowDeckCount();
		scn.ShadowChooseCards(filler1);
		assertInZone(Zone.DISCARD, filler1);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
