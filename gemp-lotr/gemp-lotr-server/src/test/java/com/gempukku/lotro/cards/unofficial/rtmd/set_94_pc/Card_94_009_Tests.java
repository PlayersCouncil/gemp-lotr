package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_009_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("troop", "1_143"); // Troop of Uruk-hai: twilight 5, qualifies
		put("butcher", "6_60"); // Berserk Butcher: twilight 6, 2nd qualifying target
		put("backstabber", "1_174"); // Goblin Backstabber: twilight 1, does not qualify
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_9"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_9", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");

		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_9
		 * Type: MetaSite
		 * Game Text: Regroup: Discard your minion of twilight cost 5 or more to add a burden.
		 */

		assertEquals("Race Text 94_9", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void DiscardingAHeavyMinionAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var troop = scn.GetShadowCard("troop");
		var butcher = scn.GetShadowCard("butcher");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveMinionsToTable(troop, butcher, backstabber);
		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction(); // Freeps acts first in the back-and-forth regroup actions

		int b = scn.GetBurdens();
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Filter specificity: only the twilight 5+ minions qualify
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop));
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(backstabber));
		scn.ShadowChooseCard(troop);

		assertInDiscard(troop);
		assertEquals(b + 1, scn.GetBurdens());

		// Control alternates to Freeps (who has nothing to do) and back to Shadow
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void UnavailableWithoutAQualifyingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void OwnerGatingFreepsCantUseEvenWithAQualifyingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var troop = scn.GetShadowCard("troop");

		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.FreepsActionAvailable(mod));
	}
}
