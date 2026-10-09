package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static org.junit.Assert.*;

public class Card_92_035_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: unique [gondor] Man companion, vitality 4
					put("aragornCopy", "1_89");  // a second copy, discarded from hand to heal the first
					put("boromir", "1_96");      // Boromir, Lord of Gondor: unique [gondor] Man companion, vitality 3
					put("athelas", "1_94");      // Athelas: [gondor] possession; Fellowship: discard it to heal a companion
					put("galadriel", "9_14");    // Galadriel, Bearer of Wisdom: [elven] companion, here only as Nenya's bearer
					put("nenya", "9_20");        // Nenya, Ring of Adamant: artifact on Galadriel; Maneuver: heal 2 companions, then discard it
					put("enquea", "1_231");      // Ulaire Enquea, Lieutenant of Morgul: a minion, so that there is a maneuver phase
					put("orophin", "1_56");      // Orophin, Lorien Bowman: [elven] Elf ally, vitality 3
					put("silinde", "1_60");      // Silinde, Elf of Mirkwood: [elven] Elf ally, vitality 2
					put("master1", "3_22");      // Master of Healing: Fellowship or Regroup event, cost 1;
					put("master2", "3_22");      //   spot 2 [elven] allies to heal a companion or ally
					put("hrethel", "6_95");      // Hrethel, Rider of Rohan: [rohan] companion, strength 6;
					                             //   Skirmish: heal a minion skirmishing Hrethel to make Hrethel strength +3 (limit +3)
					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("shaman", "1_152");      // Uruk Shaman: [isengard] Uruk-hai; Maneuver: remove (2) to heal an Uruk-hai
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_35", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: unique [gondor] Man companion, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_35"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_35
		 * Type: MetaSite
		 * Intensity: 5
		 * Game Text: While at a sanctuary, you may heal no more than one wound from each character.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_35", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(5, card.getBlueprint().getIntensity());
	}

	@Test
	public void SanctuaryHealingLimitedToOnePerCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();

		scn.SkipToSite(3);

		// Sanctuary healing at start of turn; 5 heals available.
		// First heal on Aragorn — should work
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Second heal — Aragorn should no longer be chooseable, though he still has a wound,
		// but Boromir should be
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Third heal — both already healed once, neither should be chooseable.
		// Remaining sanctuary heals should fizzle/skip.
		// We should land in fellowship phase actions.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AthelasCantHealAlreadyHealedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();

		scn.SkipToSite(3);

		// Sanctuary healing heals Aragorn once
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));

		// Now in fellowship phase. Athelas should not be able to heal Aragorn
		// (already healed once at this sanctuary).
		assertTrue(scn.FreepsActionAvailable(athelas));
		scn.FreepsUseCardAction(athelas);
		// Neither of the Athelas's alternatives has anything to act on (nobody else is wounded and
		// no Shadow condition is in play), so no choice between them is asked.
		assertInDiscard(athelas);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardToHealFizzlesAtSanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornCopy = scn.GetFreepsCard("aragornCopy");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(aragornCopy);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.SkipToSite(3);

		// Sanctuary healing heals Aragorn once
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Discard-to-heal action should be available (it checks wounds > 0, not canBeHealed)
		assertTrue(scn.FreepsHasDiscardToHealAvailable());
		scn.FreepsDiscardToHeal(aragornCopy);

		// Heal fizzles but card is still discarded (cost paid)
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealingWorksAgainAfterLeavingSanctuary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var galadriel = scn.GetFreepsCard("galadriel");
		var nenya = scn.GetFreepsCard("nenya");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCompanionsToTable(galadriel);
		scn.AttachCardsTo(galadriel, nenya);
		scn.AddWoundsToChar(frodo, 2);

		scn.StartGame();

		scn.SkipToSite(3);
		scn.MoveMinionsToTable(enquea);

		// Sanctuary healing heals Frodo once
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		// Pass fellowship phase; auto-moves to site 4 (non-sanctuary)
		scn.FreepsPass();

		scn.SkipToPhase(Phase.MANEUVER);

		// Use Nenya to heal at non-sanctuary — should not be blocked
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsUseCardAction(nenya);

		assertEquals(0, scn.GetWoundsOn(frodo));

		// Nenya's "place up to 2 cards from your hand on top of your draw deck": none.
		scn.FreepsDeclineChoosing();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AWoundHealedAwayFromTheSanctuaryEarlierInTheTurnDoesNotCountAgainstItsOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		var master1 = scn.GetFreepsCard("master1");
		var master2 = scn.GetFreepsCard("master2");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin, silinde);
		scn.MoveCardsToHand(master1, master2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(aragorn, 2);

		// Site 2 is no sanctuary: Aragorn is healed there in the fellowship phase.
		assertTrue(scn.FreepsPlayAvailable(master1));
		scn.FreepsPlayCard(master1);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fellowship moves to site 3, a sanctuary, and Aragorn is healed there in the regroup phase. The
		// first heal was not made at a sanctuary, so this is the one wound the sanctuary allows him.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsPlayAvailable(master2));
		scn.FreepsPlayCard(master2);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// When Shadow owns the modifier, Freeps healing should not be restricted
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.SkipToSite(3);

		// Sanctuary healing — Aragorn should be healable twice (no restriction)
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Nobody is left wounded, so the other three heals are not asked.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyIsHealedOfOnlyOneWoundAtASanctuaryLikeAnyCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		var master1 = scn.GetFreepsCard("master1");
		var master2 = scn.GetFreepsCard("master2");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin, silinde);
		scn.MoveCardsToHand(master1, master2);

		scn.StartGame();
		scn.SkipToSite(3);

		// Wounded only now: the rule's sanctuary healing has had nobody to ask about, so every
		// heal of this turn is a card's.
		scn.AddWoundsToChar(orophin, 2);
		scn.AddWoundsToChar(silinde, 1);
		scn.AddWoundsToChar(aragorn, 1);

		assertTrue(scn.FreepsPlayAvailable(master1));
		scn.FreepsPlayCard(master1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(orophin, silinde, aragorn));
		scn.FreepsChooseCard(orophin);
		assertEquals(1, scn.GetWoundsOn(orophin));

		// Orophin has had his one wound healed here and is offered no second; the ally and the
		// companion who have not been healed still are.
		scn.FreepsPlayCard(master2);
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, aragorn));
		scn.FreepsChooseCard(silinde);
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	@Ignore("F-0319: a minion is a character too, but minions are not counted; the bar names no player, so the engine must change first")
	public void AMinionTheOwnerHealsAtASanctuaryIsNotHealedASecondTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var hrethel = scn.GetFreepsCard("hrethel");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(hrethel);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 2);

		// The fellowship moves to site 3, a sanctuary, and fights there.
		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hrethel, savage);
		scn.FreepsResolveSkirmish(hrethel);

		// Hrethel's ability has its owner heal the minion he is skirmishing: a wound healed from a
		// character like any other.
		assertEquals(6, scn.GetStrength(hrethel));
		assertTrue(scn.FreepsActionAvailable(hrethel));
		scn.FreepsUseCardAction(hrethel);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(9, scn.GetStrength(hrethel));

		// The Savage still has a wound, but no second one may be healed from it here, so the
		// ability's cost cannot be paid.
		scn.ShadowPass();
		assertFalse(scn.FreepsActionAvailable(hrethel));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheShadowPlayersOwnHealsOfHisMinionAreNotLimited() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var shaman = scn.GetShadowCard("shaman");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(shaman, savage);
		scn.AddWoundsToChar(savage, 2);
		scn.SetTwilight(10);

		// The fellowship moves to site 3, a sanctuary. The modifier limits what its owner heals,
		// and these heals are the other player's.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(shaman));
		scn.ShadowUseCardAction(shaman);
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(shaman));
		scn.ShadowUseCardAction(shaman);
		assertEquals(0, scn.GetWoundsOn(savage));

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AWoundHealedInTheRegroupPhaseDoesNotCountAgainstTheNextTurnsSanctuaryHealing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var silinde = scn.GetFreepsCard("silinde");
		var master1 = scn.GetFreepsCard("master1");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin, silinde);
		scn.MoveCardsToHand(master1);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.AddWoundsToChar(aragorn, 2);

		// The fellowship moves to site 3, a sanctuary, and Aragorn is healed there in the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsPlayAvailable(master1));
		scn.FreepsPlayCard(master1);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The next turn begins at the same sanctuary: he may be healed of one wound again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
