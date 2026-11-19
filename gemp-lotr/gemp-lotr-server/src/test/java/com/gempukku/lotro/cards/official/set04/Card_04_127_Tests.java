package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_127_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mablung", "4_127");
					put("faramir", "4_117");      // Faramir, Son of Denethor: Ring-bound [gondor] Man, cost 3; bars only Shadow skirmish actions
					put("boromir", "0_65");       // Boromir, Steward's Heir: Ring-bound [gondor] Man; keywords only
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound Man, strength 8; a maneuver ability only

					// At site 2 every minion below is roaming but the Goblin Man.
					put("troop", "8_106");        // Siege Troop: [sauron] Orc, vitality 4, site 5; a regroup ability used only on purpose
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3, site 5; damage +1 only
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, strength 7, vitality 2, site 6; answers a weapon played on him
					// Goblin Man: an Orc, vitality 2, site 2; discarded underground, and a Shadow ability used only on purpose
					put("goblinman", "2_42");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MablungStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Mablung, Soldier of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ring-bound</b>. <b>Ranger</b>. To play, spot a Ring-bound Man.<br><b>Skirmish:</b> Exert Mablung to exhaust a roaming minion he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mablung");

		assertEquals("Mablung", card.getBlueprint().getTitle());
		assertEquals("Soldier of Gondor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilARingBoundManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mablung = scn.GetFreepsCard("mablung");
		var faramir = scn.GetFreepsCard("faramir");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(mablung, faramir);

		scn.StartGame();
		assertInHand(mablung);
		assertInHand(faramir);

		// Frodo and Sam are Ring-bound but no Men; Aragorn is a Man but unbound.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(mablung));

		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);

		assertTrue(scn.FreepsPlayAvailable(mablung));
		scn.FreepsPlayCard(mablung);
		assertInZone(Zone.FREE_CHARACTERS, mablung);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARingBoundManOfAnotherTitleLetsHimBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mablung = scn.GetFreepsCard("mablung");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(mablung);

		scn.StartGame();
		assertInHand(mablung);

		assertTrue(scn.FreepsPlayAvailable(mablung));
		scn.FreepsPlayCard(mablung);
		assertInZone(Zone.FREE_CHARACTERS, mablung);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsMablungToExhaustARoamingMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mablung = scn.GetFreepsCard("mablung");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(mablung, aragorn);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var goblinman = scn.GetShadowCard("goblinman");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, chieftain, goblinman);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{mablung, troop},
				new PhysicalCardImpl[]{aragorn, chieftain});
		// The two left over go to Mablung as well: a second roaming minion, of another culture and race, and a minion
		// who is not roaming.  Each can still exert, since a character already exhausted is no candidate for exhausting.
		scn.ShadowAssignToMinions(mablung, savage, goblinman);

		scn.FreepsResolveSkirmish(mablung);
		assertEquals(0, scn.GetWoundsOn(mablung));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(mablung));
		scn.FreepsUseCardAction(mablung);
		assertEquals(1, scn.GetWoundsOn(mablung));

		// The roaming minions he is skirmishing; not the one he is skirmishing who is not roaming, nor the roaming
		// minion waiting for Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(goblinman, chieftain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// Vitality 4: exhausted is 3 wounds (exerting twice would be 2).
		assertEquals(3, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(goblinman));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishMablungExertsAndNobodyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mablung = scn.GetFreepsCard("mablung");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(mablung, aragorn);

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, chieftain);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{mablung, savage},
				new PhysicalCardImpl[]{aragorn, chieftain});

		// Aragorn's skirmish first: Mablung's roaming minion is assigned to him and not yet skirmishing, and Aragorn's
		// is skirmishing someone else.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertTrue(scn.IsCharAssignedAgainst(mablung, savage));
		assertEquals(0, scn.GetWoundsOn(mablung));

		// The exertion can be paid; the minion is part of the effect.
		assertTrue(scn.FreepsActionAvailable(mablung));
		scn.FreepsUseCardAction(mablung);

		assertEquals(1, scn.GetWoundsOn(mablung));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithMablungExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mablung = scn.GetFreepsCard("mablung");
		scn.MoveCompanionsToTable(mablung);
		// Vitality 3 with two wounds: exhausted.
		scn.AddWoundsToChar(mablung, 2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(mablung));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(mablung, troop);
		scn.FreepsResolveSkirmish(mablung);

		assertTrue(scn.IsCharSkirmishingAgainst(mablung, troop));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertEquals(2, scn.GetWoundsOn(mablung));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mablung));
	}
}
