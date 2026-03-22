package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_024_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Úlairë Enquëa, Lieutenant of Morgul (1_231): Nazgul, twilight 6, unique
		put("enquea1", "1_231");
		put("enquea2", "1_231");
		// Orthanc Berserker, unique uruk-hai
		put("berserker1", "3_66");
		put("berserker2", "3_66");
		// Was He Dear to Thee? (103_104): Maneuver event, exert a unique [Sauron] or [Ringwraith] minion
		put("washe", "103_104");
		// Úlairë Nertëa, Messenger of Dol Guldur (51_234): errata, when played with 5+ companions,
		// play unique [wraith] minion from discard
		put("nertea", "51_234");
		// Gothmog, Lieutenant of Morgul (8_72): unique [Ringwraith] Orc, twilight 8
		put("gothmog", "8_72");
		// Dwarf Guard (1_7): Dwarven companion, not unique
		put("guard1", "1_7");
		put("guard2", "1_7");
		put("guard3", "1_7");
		put("guard4", "1_7");
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_24"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_24", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_24", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void CanPlayDuplicateNazgulWhenModifierActive() throws DecisionResultInvalidException, CardNotFoundException {
		// With the modifier, two copies of Enquëa should both be playable
		var scn = GetShadowScenario();

		var enquea1 = scn.GetShadowCard("enquea1");
		var enquea2 = scn.GetShadowCard("enquea2");

		scn.MoveMinionsToTable(enquea1);
		scn.MoveCardsToHand(enquea2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowPlayAvailable(enquea2));
		scn.ShadowPlayCard(enquea2);

		assertInZone(Zone.SHADOW_CHARACTERS, enquea1);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea2);
	}

	@Test
	public void CannotPlayDuplicateNazgulWithoutModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// Without the modifier (Freeps owns it), uniqueness is enforced normally
		var scn = GetFreepsScenario();

		var enquea1 = scn.GetShadowCard("enquea1");
		var enquea2 = scn.GetShadowCard("enquea2");

		scn.MoveMinionsToTable(enquea1);
		scn.MoveCardsToHand(enquea2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowPlayAvailable(enquea2));
	}

	@Test
	public void NonNazgulUniquenessUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		// Goblin Runner is already non-unique, so this just confirms the modifier
		// doesn't break anything for non-nazgul cards.
		// More importantly, if there were a unique non-nazgul, it should stay unique.
		var scn = GetShadowScenario();

		var berserker1 = scn.GetShadowCard("berserker1");
		var berserker2 = scn.GetShadowCard("berserker2");

		scn.MoveMinionsToTable(berserker1);
		scn.MoveCardsToHand(berserker2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowPlayAvailable(berserker2));
	}

	@Test
	public void WasHeDearCannotExertNonUniqueNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		// Was He Dear to Thee? requires exerting a unique [Sauron] or [Ringwraith] minion.
		// With 92_24 active, Enquëa is no longer unique, so the event should not be playable
		// if he's the only qualifying minion.
		var scn = GetShadowScenario();

		var enquea1 = scn.GetShadowCard("enquea1");
		var washe = scn.GetShadowCard("washe");

		scn.MoveMinionsToTable(enquea1);
		scn.MoveCardsToHand(washe);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.ShadowPlayAvailable(washe));
	}

	@Test
	public void NerteaPlaysGothmogButNotEnqueaFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		// Nertëa's trigger plays a unique [wraith] minion from discard.
		// With 92_24 active, Enquëa is non-unique and should not be a valid target.
		// Gothmog is a unique [Ringwraith] Orc (not a Nazgul), so he should still be unique
		// and auto-chosen as the only valid target.
		var scn = GetShadowScenario();

		var nertea = scn.GetShadowCard("nertea");
		var enquea1 = scn.GetShadowCard("enquea1");
		var gothmog = scn.GetShadowCard("gothmog");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		var guard4 = scn.GetFreepsCard("guard4");

		scn.MoveCompanionsToTable(guard1, guard2, guard3, guard4);
		scn.MoveCardsToDiscard(enquea1, gothmog);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.SkipToPhase(Phase.SHADOW);

		// Play Nertëa — triggers the "play unique wraith minion from discard" effect
		scn.ShadowPlayCard(nertea);
		// Accept the optional trigger
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseYes();
		// Gothmog should auto-play as the only valid unique wraith minion in discard
		assertInZone(Zone.SHADOW_CHARACTERS, gothmog);
		// Enquëa should still be in discard — no longer unique
		assertInZone(Zone.DISCARD, enquea1);
	}
}
